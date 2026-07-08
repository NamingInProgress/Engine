package com.vke.api.rendering.vulkan.descriptors.parsing;

import com.vke.api.rendering.vulkan.descriptors.parsing.node.ArrayIndexNode;
import com.vke.api.rendering.vulkan.descriptors.parsing.node.EntryNode;
import com.vke.api.rendering.vulkan.descriptors.parsing.node.Node;
import com.vke.api.rendering.vulkan.descriptors.types.ArrayType;
import com.vke.api.rendering.vulkan.descriptors.types.StructType;
import com.vke.api.rendering.vulkan.descriptors.types.TypeLayout;

public class LayoutResolver {

    public LayoutResolution resolveLayoutPath(TypeLayout layoutRoot, EntryNode treeRoot) {
        TypeLayout current = layoutRoot;
        long offset = 0;

        Node node = treeRoot;

        while (node != null) {

            if (node instanceof EntryNode entry) {
                StructType.Member m = ((StructType) current).members.get(entry.name);

                offset += m.offset;
                current = m.type;
            } else if (node instanceof ArrayIndexNode arr) {
                ArrayType arrType = ((ArrayType) current);

                offset += arr.index * arrType.stride;
                current = arrType.elementType;
            }

            node = node.child;
        }

        return new LayoutResolution(current, current, offset);
    }

    public record LayoutResolution(TypeLayout finalType, TypeLayout lastLayout, long offset) {}

}
