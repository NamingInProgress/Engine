package com.vke.core.rendering.vulkan.buffer;

import com.vke.api.vulkan.buffer.Vertex;
import com.vke.api.vulkan.buffer.VertexBuffer;
import com.vke.core.VKEngine;
import com.vke.core.rendering.vulkan.VulkanSetup;
import com.vke.core.rendering.vulkan.mem.VulkanBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.vulkan.VK14;
import org.lwjgl.vulkan.VkBufferDeviceAddressInfo;

public class VkVertexBuffer<T extends Vertex> {
    private final VulkanBuffer vkBuffer;
    private final VertexBuffer vertexBuffer;
    private long deviceAddress;

    public VkVertexBuffer(VKEngine engine, VulkanSetup setup, VertexBuffer buffer) {
        this.vertexBuffer = buffer;
        int allocSize = buffer.getByteStride() * buffer.vertexCount;
        VulkanBuffer.MemoryUsage usage = VulkanBuffer.MemoryUsage.Bits.HOST_VISIBLE.into();
        vkBuffer = new VulkanBuffer(engine, setup, allocSize, VulkanBuffer.BufferUsage.SSBO, usage);

        //try(MemoryStack stack = MemoryStack.stackPush()) {
        //    VkBufferDeviceAddressInfo addressInfo = VkBufferDeviceAddressInfo.calloc(stack)
        //            .sType$Default()
        //            .buffer(vkBuffer.getBuffer());
        //
        //    deviceAddress = VK14.vkGetBufferDeviceAddress(setup.getLogicalDevice().getDevice(), addressInfo);
        //
        //}
    }

    public void uploadToGPU() {
        MemoryUtil.memCopy(
                MemoryUtil.memAddress(vertexBuffer.getData()),
                vkBuffer.getMappedAddress(),
                (long) vertexBuffer.vertexCount * vertexBuffer.getByteStride()
        );
    }
}
