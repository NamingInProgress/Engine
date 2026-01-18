package com.vke.core.rendering.vulkan;

import com.vke.utils.Colors;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.system.Pointer;
import org.lwjgl.vulkan.EXTDebugUtils;

import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.Iterator;

public class Utils {
    public static PointerBuffer wrap(MemoryStack stack, Collection<ByteBuffer> c) {
        PointerBuffer buf = stack.mallocPointer(c.size());
        int i = 0;
        for (ByteBuffer t : c) {
            buf.put(i++, t);
        }
        buf.flip();
        return buf;
    }

    public static PointerBuffer wrapStrings(MemoryStack stack, Collection<String> strings) {
        PointerBuffer buf = stack.mallocPointer(strings.size());
        int i = 0;
        for (String s : strings) {
            ByteBuffer bb = stack.UTF8(s);
            buf.put(i++, bb);
        }
        return buf;
    }

    public static Iterator<String> unwrapStrings(PointerBuffer buffer, int count) {
        return new Iterator<>() {
            private int index = 0;

            @Override
            public boolean hasNext() {
                return index < count;
            }

            @Override
            public String next() {
                StringBuilder builder = new StringBuilder();
                long stringAddress = buffer.get(index);
                char lastChar;
                int charIndex = 0;
                do {
                    byte b = MemoryUtil.memGetByte(stringAddress + charIndex);
                    lastChar = (char) b;
                    charIndex++;
                    if (lastChar != '\0') {
                        builder.append(lastChar);
                    }
                } while (lastChar != '\0');
                index++;
                return builder.toString();
            }
        };
    }

    public static PointerBuffer mergePointers(MemoryStack stack, int[] lengths, PointerBuffer... buffs) {
        PointerBuffer buf = stack.mallocPointer(buffs.length);
        int idx = 0;
        for (int i = 0; i < buffs.length; i++) {
            int length = lengths[i];
            for (int j = 0; j < length; j++) {
                buf.put(idx++, buffs[i].get(j));
            }
        }
        return buf;
    }

    public static String getDebugMessageType(int type) {
        Colors text = new Colors("[");

        return switch (type) {
            case EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_TYPE_GENERAL_BIT_EXT -> text.cyan("GENERAL").reset("]").toString();
            case EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_TYPE_VALIDATION_BIT_EXT -> text.cyan("VALIDATION").reset("]").toString();
            case EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_TYPE_PERFORMANCE_BIT_EXT -> text.cyan("PERFORMANCE").reset("]").toString();
            default -> throw new IllegalStateException("Unexpected value: " + type);
        };
    }

}
