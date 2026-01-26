package com.vke.core.rendering.vulkan.mem;

import com.vke.api.vulkan.VkBitEnum;
import com.vke.api.vulkan.VkEnum;
import com.vke.core.VKEngine;
import com.vke.core.rendering.vulkan.VKUtils;
import com.vke.core.rendering.vulkan.VulkanSetup;
import com.vke.utils.Disposable;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

import java.nio.LongBuffer;

public class VulkanBuffer implements Disposable {
    private static final String HERE = "Buffer";

    private final long buffer, bufferMemory, mappedAddress;
    private final VkDevice device;

    public VulkanBuffer(VKEngine engine, VulkanSetup setup, long size, BufferUsage usageFlags, MemoryUsage memoryUsage) {
        try(MemoryStack stack = MemoryStack.stackPush()) {
            VkBufferCreateInfo bufferCreateInfo = VkBufferCreateInfo.calloc(stack)
                    .sType$Default()
                    .size(size)
                    .usage(usageFlags.getVkHandle())
                    .sharingMode(VK14.VK_SHARING_MODE_EXCLUSIVE);

            device = setup.getLogicalDevice().getDevice();
            LongBuffer pBuffer = stack.mallocLong(1);
            if (VK14.vkCreateBuffer(device, bufferCreateInfo, null, pBuffer) != VK14.VK_SUCCESS) {
                engine.throwException(new IllegalStateException("Couldn't create buffer object!"), HERE);
            }

            buffer = pBuffer.get(0);

            VkMemoryRequirements memRequirements = VkMemoryRequirements.malloc(stack);
            VK14.vkGetBufferMemoryRequirements(device, buffer, memRequirements);

            int memType = VKUtils.findMemoryType(setup.getLogicalDevice().getPhysicalDevice(),
                    memRequirements.memoryTypeBits(),
                    memoryUsage.getVkHandle()
            );

            if (memType == ~0) {
                engine.throwException(new IllegalStateException("No memory type found for memory usage: " + memoryUsage.mask), HERE);
            }

            VkMemoryAllocateInfo allocInfo = VkMemoryAllocateInfo.calloc(stack)
                    .sType$Default()
                    .allocationSize(memRequirements.size())
                    .memoryTypeIndex(memType);

            LongBuffer pMemory = stack.mallocLong(1);

            if (VK14.vkAllocateMemory(device, allocInfo, null, pMemory) != VK14.VK_SUCCESS) {
                engine.throwException(new IllegalStateException("Couldn't create memory object!"), HERE);
            }

            bufferMemory = pMemory.get(0);

            VK14.vkBindBufferMemory(device, buffer, bufferMemory, 0);

            PointerBuffer pData = stack.mallocPointer(1);

            VK14.vkMapMemory(device, bufferMemory, 0, size, 0, pData);

            mappedAddress = pData.get(0);
        }
    }

    public long getBuffer() {
        return buffer;
    }

    public long getBufferMemory() {
        return bufferMemory;
    }

    public long getMappedAddress() {
        return mappedAddress;
    }

    @Override
    public void free() {
        VK14.vkDestroyBuffer(device, buffer, null);
    }

    public enum BufferUsage implements VkEnum {

        TRANSFER_SRC(VK14.VK_BUFFER_USAGE_TRANSFER_SRC_BIT),
        TRANSFER_DST(VK14.VK_BUFFER_USAGE_TRANSFER_DST_BIT),
        UNIFORM_TEXEL_BUFFER(VK14.VK_BUFFER_USAGE_UNIFORM_TEXEL_BUFFER_BIT),
        STORAGE_TEXEL_BUFFER(VK14.VK_BUFFER_USAGE_STORAGE_TEXEL_BUFFER_BIT),
        UBO(VK14.VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT),
        SSBO(VK14.VK_BUFFER_USAGE_STORAGE_BUFFER_BIT),
        EBO(VK14.VK_BUFFER_USAGE_INDEX_BUFFER_BIT),
        VBO(VK14.VK_BUFFER_USAGE_VERTEX_BUFFER_BIT),
        INDIRECT_BUFFER(VK14.VK_BUFFER_USAGE_INDIRECT_BUFFER_BIT);

        private final int vkHandle;

        BufferUsage(int vkHandle) {
            this.vkHandle = vkHandle;
        }

        @Override
        public int getVkHandle() {
            return vkHandle;
        }
    }

    public static class MemoryUsage implements VkBitEnum<MemoryUsage, MemoryUsage.Bits> {
        private int mask;

        public MemoryUsage(Bits... bits) {
            or(bits);
        }

        @Override
        public MemoryUsage or(Bits... flags) {
            for (Bits bit : flags) {
                mask |= bit.getVkHandle();
            }
            return this;
        }

        @Override
        public int getVkHandle() {
            return mask;
        }

        public enum Bits implements VkEnum {
            DEVICE_LOCAL(VK14.VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT),
            HOST_VISIBLE(VK14.VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT),
            HOST_COHERENT(VK14.VK_MEMORY_PROPERTY_HOST_COHERENT_BIT),
            HOST_CACHED(VK14.VK_MEMORY_PROPERTY_HOST_CACHED_BIT),
            LAZILY_ALLOCATED(VK14.VK_MEMORY_PROPERTY_LAZILY_ALLOCATED_BIT);

            private final int vkHandle;

            Bits(int vkHandle) {
                this.vkHandle = vkHandle;
            }

            @Override
            public int getVkHandle() {
                return vkHandle;
            }

            public MemoryUsage into() {
                return new MemoryUsage(this);
            }
        }
    }

}