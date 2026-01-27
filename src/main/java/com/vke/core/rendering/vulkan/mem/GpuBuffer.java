package com.vke.core.rendering.vulkan.mem;

import com.vke.api.vulkan.VkBitEnum;
import com.vke.api.vulkan.VkEnum;
import com.vke.core.VKEngine;
import com.vke.core.rendering.vulkan.VulkanSetup;
import com.vke.utils.Disposable;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.vma.Vma;
import org.lwjgl.util.vma.VmaAllocationCreateInfo;
import org.lwjgl.util.vma.VmaAllocationInfo;
import org.lwjgl.vulkan.VK14;
import org.lwjgl.vulkan.VkBufferCreateInfo;
import org.lwjgl.vulkan.VkDevice;

import java.nio.LongBuffer;

public class GpuBuffer implements Disposable {
    private static final String HERE = "Buffer";

    private final long allocator;
    private final long buffer, allocation;
    private final VmaAllocationInfo info;
    private final VkDevice device;

    public GpuBuffer(VKEngine engine, VulkanSetup setup, long size, BufferUsage usageFlags, MemoryUsage memoryUsage) {
        try(MemoryStack stack = MemoryStack.stackPush()) {
            VkBufferCreateInfo bufferCreateInfo = VkBufferCreateInfo.calloc(stack)
                    .sType$Default()
                    .size(size)
                    .usage(usageFlags.getVkHandle())
                    .sharingMode(VK14.VK_SHARING_MODE_EXCLUSIVE);

            VmaAllocationCreateInfo allocationCreateInfo = VmaAllocationCreateInfo.calloc(stack)
                    .usage(memoryUsage.getVkHandle())
                    .flags(Vma.VMA_ALLOCATION_CREATE_MAPPED_BIT);

            LongBuffer pBuffer = stack.mallocLong(1);
            PointerBuffer pAllocation = stack.mallocPointer(1);
            VmaAllocationInfo allocationInfo = VmaAllocationInfo.calloc();

            device = setup.getLogicalDevice().getDevice();
            if (Vma.vmaCreateBuffer(setup.getVmaAllocator(), bufferCreateInfo, allocationCreateInfo, pBuffer, pAllocation, allocationInfo) != VK14.VK_SUCCESS) {
                engine.throwException(new IllegalStateException("Unable to allocate mapped gpu memory"), HERE);
            }

            allocator = setup.getVmaAllocator();
            buffer = pBuffer.get(0);
            allocation = pAllocation.get(0);
            info = allocationInfo;
        }
    }

    public long getBuffer() {
        return buffer;
    }

    public long getAllocation() {
        return allocation;
    }

    public VmaAllocationInfo getInfo() {
        return info;
    }

    @Override
    public void free() {
        Vma.vmaDestroyBuffer(allocator, buffer, allocation);
        info.free();
    }

    public static class BufferUsage implements VkBitEnum<BufferUsage, BufferUsage.Bits> {

        private int mask;

        public BufferUsage(Bits... bits) { or(bits); }

        @Override
        public BufferUsage or(Bits... flags) {
            for (Bits bit : flags) {
                mask |= bit.getVkHandle();
            }
            return this;
        }

        @Override
        public int getVkHandle() {
            return this.mask;
        }

        public enum Bits implements VkEnum {

            TRANSFER_SRC(VK14.VK_BUFFER_USAGE_TRANSFER_SRC_BIT),
            TRANSFER_DST(VK14.VK_BUFFER_USAGE_TRANSFER_DST_BIT),
            UNIFORM_TEXEL_BUFFER(VK14.VK_BUFFER_USAGE_UNIFORM_TEXEL_BUFFER_BIT),
            STORAGE_TEXEL_BUFFER(VK14.VK_BUFFER_USAGE_STORAGE_TEXEL_BUFFER_BIT),
            UBO(VK14.VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT),
            SSBO(VK14.VK_BUFFER_USAGE_STORAGE_BUFFER_BIT),
            IBO(VK14.VK_BUFFER_USAGE_INDEX_BUFFER_BIT),
            VBO(VK14.VK_BUFFER_USAGE_VERTEX_BUFFER_BIT),
            INDIRECT_BUFFER(VK14.VK_BUFFER_USAGE_INDIRECT_BUFFER_BIT),
            SHADER_DEVICE_ADDRESS(VK14.VK_BUFFER_USAGE_SHADER_DEVICE_ADDRESS_BIT);

            private final int vkHandle;

            Bits(int vkHandle) {
                this.vkHandle = vkHandle;
            }

            @Override
            public int getVkHandle() {
                return vkHandle;
            }

            public BufferUsage into() { return new BufferUsage(this); }
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
            UNKNOWN(Vma.VMA_MEMORY_USAGE_UNKNOWN),
            GPU_ONLY(Vma.VMA_MEMORY_USAGE_GPU_ONLY),
            CPU_ONLY(Vma.VMA_MEMORY_USAGE_CPU_ONLY),
            CPU_TO_GPU(Vma.VMA_MEMORY_USAGE_CPU_TO_GPU),
            GPU_TO_CPU(Vma.VMA_MEMORY_USAGE_GPU_TO_CPU),
            CPU_COPY(Vma.VMA_MEMORY_USAGE_CPU_COPY),
            GPU_LAZILY_ALLOCATED(Vma.VMA_MEMORY_USAGE_GPU_LAZILY_ALLOCATED),
            AUTO(Vma.VMA_MEMORY_USAGE_AUTO),
            AUTO_PREFER_DEVICE(Vma.VMA_MEMORY_USAGE_AUTO_PREFER_DEVICE),
            AUTO_PREFER_HOST(Vma.VMA_MEMORY_USAGE_AUTO_PREFER_HOST);

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