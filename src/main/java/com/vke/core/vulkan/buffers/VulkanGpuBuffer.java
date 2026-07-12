package com.vke.core.vulkan.buffers;

import com.vke.api.rendering.abstraction.data.GpuBuffer;
import com.vke.api.rendering.abstraction.enums.buffer.BufferUsage;
import com.vke.api.rendering.abstraction.enums.buffer.MemoryUsage;
import com.vke.core.VKEngine;
import com.vke.core.vulkan.device.VulkanRenderDevice;
import com.vke.core.vulkan.utils.VKUtils;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.vma.Vma;
import org.lwjgl.util.vma.VmaAllocationCreateInfo;
import org.lwjgl.util.vma.VmaAllocationInfo;
import org.lwjgl.vulkan.VK14;
import org.lwjgl.vulkan.VkBufferCreateInfo;
import org.lwjgl.vulkan.VkDevice;

import java.nio.LongBuffer;
import java.util.Arrays;

public class VulkanGpuBuffer implements GpuBuffer {
    private static final String HERE = "Buffer@VulkanImpl/GPUBuffer";

    public static int counter = 0;

    private final long allocator;
    private final long buffer, allocation;
    private final VmaAllocationInfo info;
    private final VkDevice device;

    private boolean free = false;

    private final long size;
    private final BufferUsage usage;
    private final MemoryUsage memUsage;

    private int idx;

    public VulkanGpuBuffer(VKEngine engine, VulkanRenderDevice device, Description info) {
        this(engine, device, info.size(), info.usage(), info.memUsage(), info.flags());
    }

    private VulkanGpuBuffer(VKEngine engine, VulkanRenderDevice rd, long size, BufferUsage usageFlags, MemoryUsage memoryUsage, int... flags) {
        this.size = size;
        this.usage = usageFlags;
        this.memUsage = memoryUsage;

        try(MemoryStack stack = MemoryStack.stackPush()) {
            VkBufferCreateInfo bufferCreateInfo = VkBufferCreateInfo.calloc(stack)
                    .sType$Default()
                    .size(size)
                    .usage(usageFlags.getVkHandle())
                    .sharingMode(VK14.VK_SHARING_MODE_EXCLUSIVE);

            VmaAllocationCreateInfo allocationCreateInfo = VmaAllocationCreateInfo.calloc(stack)
                    .usage(memoryUsage.getVkHandle())
                    .flags(Vma.VMA_ALLOCATION_CREATE_MAPPED_BIT | Arrays.stream(flags).reduce(0, (a, b) -> a | b));

            LongBuffer pBuffer = stack.mallocLong(1);
            PointerBuffer pAllocation = stack.mallocPointer(1);
            VmaAllocationInfo allocationInfo = VmaAllocationInfo.calloc();

            device = rd.getLogicalDevice().getDevice();
            if (Vma.vmaCreateBuffer(rd.getVmaAllocator(), bufferCreateInfo, allocationCreateInfo, pBuffer, pAllocation, allocationInfo) != VK14.VK_SUCCESS) {
                engine.throwException(new IllegalStateException("Unable to allocate mapped gpu memory"), HERE);
            }

            allocator = rd.getVmaAllocator();
            buffer = pBuffer.get(0);
            allocation = pAllocation.get(0);
            info = allocationInfo;

            if (engine.isDebugMode()) {
                this.idx = counter;
                VKUtils.setDebugName(rd.getLogicalDevice(), "Gpu Buffer #" + counter, this.getBuffer(), VK14.VK_OBJECT_TYPE_BUFFER);
                counter++;
            }
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
        if (!free) {
            Vma.vmaDestroyBuffer(allocator, buffer, allocation);
            info.free();
            free = true;
        }
    }

    @Override
    public long size() {
        return size;
    }

    @Override
    public BufferUsage usage() {
        return usage;
    }

    @Override
    public MemoryUsage memoryUsage() {
        return memUsage;
    }

}