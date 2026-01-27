package com.vke.core.rendering.vulkan.buffer;

import com.vke.api.vulkan.buffer.CpuBuffer;
import com.vke.core.VKEngine;
import com.vke.core.rendering.vulkan.VulkanRenderer;
import com.vke.core.rendering.vulkan.VulkanSetup;
import com.vke.core.rendering.vulkan.commands.CommandBuffers;
import com.vke.core.rendering.vulkan.mem.GpuBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.vulkan.VK14;
import org.lwjgl.vulkan.VkBufferCopy;
import org.lwjgl.vulkan.VkCommandBuffer;

public class AllocatedBuffer {
    private final GpuBuffer gpuBuffer;
    private final CpuBuffer cpuBuffer;

    public AllocatedBuffer(VKEngine engine, VulkanSetup setup, CpuBuffer buffer, GpuBuffer.BufferUsage usage, GpuBuffer.MemoryUsage memoryUsage) {
        this.cpuBuffer = buffer;
        int allocSize = buffer.getByteStride() * buffer.elementCount;
        gpuBuffer = new GpuBuffer(engine, setup, allocSize, usage, memoryUsage);
    }

    public void uploadViaStaging(VKEngine engine, VulkanSetup setup) {
        long size = cpuBuffer.getSizeBytes();

        GpuBuffer.BufferUsage bufUsage = new GpuBuffer.BufferUsage(
                GpuBuffer.BufferUsage.Bits.TRANSFER_SRC
        );

        GpuBuffer.MemoryUsage memUsage = new GpuBuffer.MemoryUsage(
                GpuBuffer.MemoryUsage.Bits.CPU_TO_GPU
        );

        GpuBuffer staging = new GpuBuffer(engine, setup, size, bufUsage, memUsage);

        long gpuAddress = staging.getInfo().pMappedData();
        long cpuAddress = cpuBuffer.getAddress();
        MemoryUtil.memCopy(cpuAddress, gpuAddress, size);

        VulkanRenderer renderer = engine.getRenderer();
        renderer.immediateSubmit((MemoryStack stack, CommandBuffers vkeCmd) -> {
            VkCommandBuffer cmd = vkeCmd.getBuffer();

            VkBufferCopy.Buffer pRegions = VkBufferCopy.calloc(1, stack);
            pRegions.get(0)
                    .size(size)
                    .srcOffset(0)
                    .dstOffset(0);

            VK14.vkCmdCopyBuffer(cmd, staging.getBuffer(), gpuBuffer.getBuffer(), pRegions);
        });

        staging.free();
    }

    public GpuBuffer getMappedBuffer() {
        return gpuBuffer;
    }

    public CpuBuffer getBuffer() {
        return cpuBuffer;
    }
}
