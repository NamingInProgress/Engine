package com.vke.core.rendering.vulkan.buffers;

import com.vke.api.rendering.abstraction.renderer.commands.ImmediateSubmit;
import com.vke.api.rendering.abstraction.renderer.data.GpuBuffer;
import com.vke.api.rendering.abstraction.renderer.data.IRenderAllocator;
import com.vke.api.rendering.vulkan.buffer.CpuBuffer;
import com.vke.api.rendering.abstraction.renderer.enums.buffer.BufferUsage;
import com.vke.api.rendering.abstraction.renderer.enums.buffer.MemoryUsage;
import com.vke.core.rendering.vulkan.service.VulkanRenderSystem;
import com.vke.core.rendering.vulkan.command.VulkanCmdBuffers;
import com.vke.utils.io.Disposable;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.vulkan.VK14;
import org.lwjgl.vulkan.VkBufferCopy;

import java.util.Objects;

public class StagedBuffer implements Disposable {
    private final VulkanGpuBuffer gpuBuffer;
    private final CpuBuffer cpuBuffer;
    private final VulkanRenderSystem vkCtx;

    public StagedBuffer(VulkanRenderSystem vkCtx, CpuBuffer buffer,
                        BufferUsage usage, MemoryUsage memoryUsage) {
        this.cpuBuffer = buffer;
        this.vkCtx = vkCtx;
        int allocSize = buffer.getByteStride() * buffer.elementCount;
        gpuBuffer = vkCtx.device().createBuffer(new GpuBuffer.Description(allocSize, usage.or(BufferUsage.Bits.TRANSFER_DST), memoryUsage));
    }

    public void uploadViaStaging() {
        try (ImmediateSubmit submit = vkCtx.device().immediateSubmit()) {
            uploadViaStaging(submit);
        }
    }

    public void uploadViaStaging(ImmediateSubmit submit) {
        uploadViaStaging(submit.allocator(), (VulkanCmdBuffers) submit.cmd());
    }

    public void uploadViaStaging(IRenderAllocator allocator, VulkanCmdBuffers cmd) {
        long size = cpuBuffer.getSizeBytes();

        BufferUsage bufUsage = new BufferUsage(
                BufferUsage.Bits.TRANSFER_SRC
        );

        MemoryUsage memUsage = new MemoryUsage(
                MemoryUsage.Bits.CPU_TO_GPU
        );

        VulkanGpuBuffer staging = (VulkanGpuBuffer) vkCtx.device().createBufferManaged(allocator, new GpuBuffer.Description(size, bufUsage, memUsage));

        long gpuAddress = staging.getInfo().pMappedData();
        long cpuAddress = cpuBuffer.getAddress();
        MemoryUtil.memCopy(cpuAddress, gpuAddress, size);

        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkBufferCopy.Buffer pRegions = VkBufferCopy.calloc(1, stack);
            pRegions.get(0)
                    .size(size)
                    .srcOffset(0)
                    .dstOffset(0);

            VK14.vkCmdCopyBuffer(cmd.getBuffer(), staging.getBuffer(), gpuBuffer.getBuffer(), pRegions);
        }
    }

    public VulkanGpuBuffer getGpuBuffer() {
        return gpuBuffer;
    }

    public CpuBuffer getCpuBuffer() {
        return cpuBuffer;
    }

    @Override
    public void free() {
        gpuBuffer.free();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        StagedBuffer that = (StagedBuffer) o;
        return Objects.equals(gpuBuffer, that.gpuBuffer) && Objects.equals(cpuBuffer, that.cpuBuffer);
    }

    @Override
    public int hashCode() {
        return Objects.hash(gpuBuffer, cpuBuffer);
    }
}
