package com.vke.core.rendering.vulkan;

import com.vke.core.memory.AutoHeapAllocator;
import com.vke.core.rendering.vulkan.device.PhysicalDevice;
import com.vke.utils.Colors;
import com.vke.utils.Utils;
import org.lwjgl.PointerBuffer;
import org.lwjgl.glfw.GLFWVulkan;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.vulkan.*;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

public class VKUtils {
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

    public static <T> Collection<T> collectIter(Iterator<T> iter, int sizeHint) {
        ArrayList<T> list;
        if (sizeHint >= 0) {
            list = new ArrayList<>(sizeHint);
        } else {
            list = new ArrayList<>();
        }
        iter.forEachRemaining(list::add);
        return list;
    }

    public static Iterator<String> getGlfwExtensionNames(MemoryStack stack) {
        PointerBuffer glfwExtensions = GLFWVulkan.glfwGetRequiredInstanceExtensions();
        if (glfwExtensions == null) {
            glfwExtensions = stack.callocPointer(0);
        }
        return VKUtils.unwrapStrings(glfwExtensions, glfwExtensions.capacity());
    }

    public static String getGpuName(MemoryStack stack, VkPhysicalDevice device) {
        VkPhysicalDeviceProperties p = VkPhysicalDeviceProperties.calloc(stack);
        VK14.vkGetPhysicalDeviceProperties(device, p);
        return p.deviceNameString();
    }

    public static VkExtent2D clampExtent(MemoryStack stack, int width, int height, VkExtent2D min, VkExtent2D max) {
        VkExtent2D extent = VkExtent2D.calloc(stack);
        extent.width(Math.clamp(width, min.width(), max.width()));
        extent.height(Math.clamp(height, min.height(), max.height()));
        return extent;
    }

    public static VkExtent2D clampExtent(AutoHeapAllocator alloc, int width, int height, VkExtent2D min, VkExtent2D max) {
        VkExtent2D extent = alloc.allocStruct(VkExtent2D.SIZEOF, VkExtent2D::new);
        extent.width(Math.clamp(width, min.width(), max.width()));
        extent.height(Math.clamp(height, min.height(), max.height()));
        return extent;
    }

    public static boolean isPresentQueue(MemoryStack stack, PhysicalDevice physicalDevice, int index, long surfaceHandle) {
        IntBuffer output = stack.mallocInt(1);
        KHRSurface.vkGetPhysicalDeviceSurfaceSupportKHR(physicalDevice.getDevice(), index, surfaceHandle, output);
        return output.get(0) == 1;
    }

    public static String readUTF8(ByteBuffer b) {
        byte[] bytes = Utils.acquireByteArrayFromBuffer(b);
        if ((bytes[bytes.length - 1] & 0xFF) == '\0') {
            return new String(bytes, 0, bytes.length - 1, StandardCharsets.UTF_8);
        }
        return new String(bytes, StandardCharsets.UTF_8);
    }
}
