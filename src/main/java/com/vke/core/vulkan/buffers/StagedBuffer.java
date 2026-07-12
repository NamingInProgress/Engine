package com.vke.core.vulkan.buffers;

import com.vke.api.rendering.abstraction.data.GpuBuffer;
import com.vke.api.rendering.vulkan.buffer.CpuBuffer;
import com.vke.core.VKEngine;
import com.vke.api.rendering.abstraction.enums.buffer.BufferUsage;
import com.vke.api.rendering.abstraction.enums.buffer.MemoryUsage;
import com.vke.core.services2.Services;
import com.vke.core.vulkan.service.VulkanRenderer;
import com.vke.core.vulkan.command.VulkanCmdBuffers;
import com.vke.core.vulkan.device.VulkanRenderDevice;
import com.vke.utils.io.Disposable;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.vulkan.VK14;
import org.lwjgl.vulkan.VkBufferCopy;
import org.lwjgl.vulkan.VkCommandBuffer;

import java.util.concurrent.atomic.AtomicReference;

public class StagedBuffer implements Disposable {
    private final VulkanGpuBuffer gpuBuffer;
    private final CpuBuffer cpuBuffer;
    private final VKEngine engine;
    private final VulkanRenderDevice device;

    public StagedBuffer(VKEngine engine, VulkanRenderDevice device, CpuBuffer buffer,
                        BufferUsage usage, MemoryUsage memoryUsage) {
        this.cpuBuffer = buffer;
        this.engine = engine;
        this.device = device;
        int allocSize = buffer.getByteStride() * buffer.elementCount;
        gpuBuffer = device.createBuffer(new GpuBuffer.Description(allocSize, usage, memoryUsage));
    }

    public void uploadViaStaging(Runnable postUpload) {
        device.getRenderer().immediateSubmit((stack, cmd) -> {
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

        VulkanGpuBuffer staging = device.createBuffer(new GpuBuffer.Description(size, bufUsage, memUsage));

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
