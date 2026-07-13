package com.vke.core.vulkan.buffers;

import com.vke.api.rendering.abstraction.data.GpuBuffer;
import com.vke.api.rendering.vulkan.buffer.CpuBuffer;
import com.vke.api.rendering.abstraction.enums.buffer.BufferUsage;
import com.vke.api.rendering.abstraction.enums.buffer.MemoryUsage;
import com.vke.core.vulkan.service.VulkanRenderSystem;
import com.vke.core.vulkan.command.VulkanCmdBuffers;
import com.vke.utils.io.Disposable;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.vulkan.VK14;
import org.lwjgl.vulkan.VkBufferCopy;

public class StagedBuffer implements Disposable {
    private final VulkanGpuBuffer gpuBuffer;
    private final CpuBuffer cpuBuffer;
    private final VulkanRenderSystem vkCtx;

    public StagedBuffer(VulkanRenderSystem vkCtx, CpuBuffer buffer,
                        BufferUsage usage, MemoryUsage memoryUsage) {
        this.cpuBuffer = buffer;
        this.vkCtx = vkCtx;
        int allocSize = buffer.getByteStride() * buffer.elementCount;
        gpuBuffer = vkCtx.device().createBuffer(new GpuBuffer.Description(allocSize, usage, memoryUsage));
    }

    public void uploadViaStaging(Runnable postUpload) {
        vkCtx.device().getRenderer().immediateSubmit((stack, cmd) -> {
            return uploadViaStaging(postUpload, stack, cmd);
        });
    }

    public Runnable uploadViaStaging(Runnable postUpload, MemoryStack stack, VulkanCmdBuffers cmd) {
        long size = cpuBuffer.getSizeBytes();

        BufferUsage bufUsage = new BufferUsage(
                BufferUsage.Bits.TRANSFER_SRC
        );

        MemoryUsage memUsage = new MemoryUsage(
                MemoryUsage.Bits.CPU_TO_GPU
        );

        VulkanGpuBuffer staging = vkCtx.device().createBuffer(new GpuBuffer.Description(size, bufUsage, memUsage));

        long gpuAddress = staging.getInfo().pMappedData();
        long cpuAddress = cpuBuffer.getAddress();
        MemoryUtil.memCopy(cpuAddress, gpuAddress, size);

        VkBufferCopy.Buffer pRegions = VkBufferCopy.calloc(1, stack);
        pRegions.get(0)
                .size(size)
                .srcOffset(0)
                .dstOffset(0);

        VK14.vkCmdCopyBuffer(cmd.getBuffer(), staging.getBuffer(), gpuBuffer.getBuffer(), pRegions);
        return () -> {
            staging.free();
            postUpload.run();
        };
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

}
