package com.vke.core.vulkan.buffers;

import com.vke.api.rendering.abstraction.data.Buffer;
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

public class StagedBuffer implements Disposable {
    private final GpuBuffer gpuBuffer;
    private final CpuBuffer cpuBuffer;

    public StagedBuffer(VKEngine engine, VulkanRenderDevice device, CpuBuffer buffer,
                        BufferUsage usage, MemoryUsage memoryUsage) {
        this.cpuBuffer = buffer;
        int allocSize = buffer.getByteStride() * buffer.elementCount;
        gpuBuffer = device.createBuffer(new Buffer.Description(allocSize, usage, memoryUsage));
    }

    public void uploadViaStaging(VKEngine engine, VulkanRenderDevice device, Runnable postUpload) {
        long size = cpuBuffer.getSizeBytes();

        BufferUsage bufUsage = new BufferUsage(
                BufferUsage.Bits.TRANSFER_SRC
        );

        MemoryUsage memUsage = new MemoryUsage(
                MemoryUsage.Bits.CPU_TO_GPU
        );

        GpuBuffer staging = device.createBuffer(new Buffer.Description(size, bufUsage, memUsage));

        long gpuAddress = staging.getInfo().pMappedData();
        long cpuAddress = cpuBuffer.getAddress();
        MemoryUtil.memCopy(cpuAddress, gpuAddress, size);

        VulkanRenderer renderer = engine.service(Services.VULKAN_RENDERER);
        renderer.immediateSubmit((MemoryStack stack, VulkanCmdBuffers vkeCmd) -> {
            VkCommandBuffer cmd = vkeCmd.getBuffer();

            VkBufferCopy.Buffer pRegions = VkBufferCopy.calloc(1, stack);
            pRegions.get(0)
                    .size(size)
                    .srcOffset(0)
                    .dstOffset(0);

            VK14.vkCmdCopyBuffer(cmd, staging.getBuffer(), gpuBuffer.getBuffer(), pRegions);
        }, () -> {
            staging.free();
            postUpload.run();
        });
    }

    public GpuBuffer getGpuBuffer() {
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
