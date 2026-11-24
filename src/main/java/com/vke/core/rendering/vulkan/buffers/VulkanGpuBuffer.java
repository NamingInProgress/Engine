package com.vke.core.rendering.vulkan.buffers;

import com.vke.api.rendering.abstraction.renderer.data.GpuBuffer;
import com.vke.api.rendering.abstraction.renderer.enums.buffer.BufferUsage;
import com.vke.api.rendering.abstraction.renderer.enums.buffer.MemoryUsage;
import com.vke.api.rendering.vulkan.memory.VulkanBufferBarrier;
import com.vke.core.rendering.vulkan.command.VulkanCmdBuffers;
import com.vke.core.rendering.vulkan.service.VulkanRenderSystem;
import com.vke.core.rendering.vulkan.utils.VKUtils;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.vma.Vma;
import org.lwjgl.util.vma.VmaAllocationCreateInfo;
import org.lwjgl.util.vma.VmaAllocationInfo;
import org.lwjgl.vulkan.*;

import java.nio.LongBuffer;
import java.util.Arrays;
import java.util.Objects;

public class VulkanGpuBuffer implements GpuBuffer {
    private static final String HERE = "Buffer@VulkanImpl/GPUBuffer";

    public static int counter = 0;

    private final long allocator;
    private final long buffer, allocation;
    private final VmaAllocationInfo info;

    private boolean free = false;

    private final long size;
    private final BufferUsage usage;
    private final MemoryUsage memUsage;
    private final VulkanRenderSystem sys;

    private int idx;

    public VulkanGpuBuffer(VulkanRenderSystem vkCtx, Description info) {
        this(vkCtx, info.size(), info.usage(), info.memUsage(), info.flags());
    }

    private VulkanGpuBuffer(VulkanRenderSystem vkCtx, long size, BufferUsage usageFlags, MemoryUsage memoryUsage, int... flags) {
        this.sys = vkCtx;
        this.size = size;
        this.usage = usageFlags;
        this.memUsage = memoryUsage;

        try(MemoryStack stack = MemoryStack.stackPush()) {
            VkBufferCreateInfo bufferCreateInfo = VkBufferCreateInfo.calloc(stack)
                    .sType$Default()
                    .size(size)
                    .usage(usageFlags.getIntVal())
                    .sharingMode(VK14.VK_SHARING_MODE_EXCLUSIVE);

            VmaAllocationCreateInfo allocationCreateInfo = VmaAllocationCreateInfo.calloc(stack)
                    .usage(memoryUsage.getIntVal())
                    .flags(Vma.VMA_ALLOCATION_CREATE_MAPPED_BIT | Arrays.stream(flags).reduce(0, (a, b) -> a | b));

            LongBuffer pBuffer = stack.mallocLong(1);
            PointerBuffer pAllocation = stack.mallocPointer(1);
            VmaAllocationInfo allocationInfo = VmaAllocationInfo.calloc();

            if (Vma.vmaCreateBuffer(vkCtx.device().getVmaAllocator(), bufferCreateInfo, allocationCreateInfo, pBuffer, pAllocation, allocationInfo) != VK14.VK_SUCCESS) {
                vkCtx.throwException(new IllegalStateException("Unable to allocate mapped gpu memory"), HERE);
            }

            allocator = vkCtx.device().getVmaAllocator();
            buffer = pBuffer.get(0);
            allocation = pAllocation.get(0);
            info = allocationInfo;

            if (vkCtx.getEngine().isDebugMode()) {
                this.idx = counter;
                VKUtils.setDebugName(vkCtx, "Gpu Buffer #" + counter, this.getBuffer(), VK14.VK_OBJECT_TYPE_BUFFER);
                counter++;
            }
        }
    }

    public void barrier(VulkanBufferBarrier barrier) {
        barrier(sys.getCurrentCommandBuffer(), barrier);
    }

    public void barrier(VulkanCmdBuffers buf, VulkanBufferBarrier barrier) { // TODO: Move into command buffers?
        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkBufferMemoryBarrier2.Buffer b =
                    VkBufferMemoryBarrier2.calloc(1, stack);

            b.get(0)
                    .sType$Default()
                    .srcStageMask(barrier.barrier().srcStage())
                    .srcAccessMask(barrier.barrier().srcAccess())
                    .dstStageMask(barrier.barrier().dstStage())
                    .dstAccessMask(barrier.barrier().dstAccess())
                    .srcQueueFamilyIndex(barrier.barrier().srcQueueFamily())
                    .dstQueueFamilyIndex(barrier.barrier().dstQueueFamily())
                    .buffer(getBuffer())
                    .offset(barrier.offset())
                    .size(barrier.size());

            VkDependencyInfo dependency =
                    VkDependencyInfo.calloc(stack)
                            .sType$Default()
                            .pBufferMemoryBarriers(b);

            VK14.vkCmdPipelineBarrier2(
                    buf.getBuffer(),
                    dependency
            );
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

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        VulkanGpuBuffer that = (VulkanGpuBuffer) o;
        return buffer == that.buffer && allocation == that.allocation && size == that.size && idx == that.idx && Objects.equals(usage, that.usage) && Objects.equals(memUsage, that.memUsage);
    }

    @Override
    public int hashCode() {
        return Objects.hash(buffer, allocation, size, usage, memUsage, idx);
    }
}