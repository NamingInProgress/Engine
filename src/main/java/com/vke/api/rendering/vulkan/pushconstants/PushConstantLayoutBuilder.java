package com.vke.api.rendering.vulkan.pushconstants;

import com.vke.api.rendering.vulkan.descriptors.types.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class PushConstantLayoutBuilder {

    public static List<PCField> build(TypeLayout rootLayout) {
        List<PCField> fields = new ArrayList<>();
        flatten(rootLayout, 0, fields);

        fields.sort(Comparator.comparingLong(f -> f.offset));
        return fields;
    }

    private static void flatten(TypeLayout layout, long baseOffset, List<PCField> out) {
        if (layout instanceof PrimitiveType || layout instanceof PointerType || layout instanceof MatrixType) {
            out.add(new PCField(baseOffset, layout.size, layout));
        } else if (layout instanceof ArrayType at) {
            if (at.length == -1) {
                throw new RuntimeException("Runtime-sized arrays not allowed in push constants");
            }
            for (int i = 0; i < at.length; i++) {
                long elementOffset = baseOffset + i * at.stride;
                flatten(at.elementType, elementOffset, out);
            }
        } else if (layout instanceof StructType st) {
            for (StructType.Member m : st.members.values()) {
                flatten(m.type, baseOffset + m.offset, out);
            }
        }
    }

    public static class PCField {
        public final long offset;
        public final long size;
        public final TypeLayout type;

        public PCField(long offset, long size, TypeLayout type) {
            this.offset = offset;
            this.size = size;
            this.type = type;
        }
    }

}
