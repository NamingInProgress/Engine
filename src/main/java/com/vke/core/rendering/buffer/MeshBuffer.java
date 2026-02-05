package com.vke.core.rendering.buffer;

import com.vke.api.vulkan.buffer.Vertex;
import com.vke.core.VKEngine;
import com.vke.core.rendering.vulkan.VKUtils;
import com.vke.core.rendering.vulkan.VulkanRenderer;
import com.vke.core.rendering.vulkan.VulkanSetup;
import com.vke.core.rendering.vulkan.buffer.AllocatedBuffer;
import com.vke.core.rendering.vulkan.mem.GpuBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK14;
import org.lwjgl.vulkan.VkBufferDeviceAddressInfo;
import org.lwjgl.vulkan.VkDevice;

import java.util.Arrays;

public class MeshBuffer<T extends Vertex> {
    private AllocatedBuffer vertices;
    private AllocatedBuffer indices;
    private long verticesDeviceAddress;

    private MeshBuffer() {}

    public static <T extends Vertex> MeshBuffer<T> uploadOnce(VKEngine engine, VulkanRenderer renderer, T[] vertices, int[] indices) {
        VulkanSetup setup = renderer.getSetup();
        if (vertices.length == 0) {
            engine.throwException(new IllegalStateException("Tried to upload empty buffer"), "MeshBuffer");
        }
        try(MemoryStack stack = MemoryStack.stackPush()) {
            MeshBuffer<T> self = new MeshBuffer<>();

            T template = vertices[0];

            StaticVertexBuffer<T> vbo = new StaticVertexBuffer<>(template, Arrays.asList(vertices));

            GpuBuffer.BufferUsage vertexBufUsage = new GpuBuffer.BufferUsage(
                    GpuBuffer.BufferUsage.Bits.SSBO,
                    GpuBuffer.BufferUsage.Bits.TRANSFER_DST,
                    GpuBuffer.BufferUsage.Bits.SHADER_DEVICE_ADDRESS
            );
            GpuBuffer.MemoryUsage vertexMemUsage = new GpuBuffer.MemoryUsage(
                    GpuBuffer.MemoryUsage.Bits.GPU_ONLY
            );
            self.vertices = new AllocatedBuffer(engine, setup, vbo, vertexBufUsage, vertexMemUsage);
            if (!VKUtils.setDebugName(renderer.getSetup().getLogicalDevice(), "Verts", self.vertices.getGpuBuffer().getBuffer(), VK14.VK_OBJECT_TYPE_BUFFER)) {
                engine.throwException(new IllegalStateException("Couldn't set debug name"), "asd");
            }

            VkBufferDeviceAddressInfo deviceAddressInfo = VkBufferDeviceAddressInfo.calloc(stack)
                    .sType$Default()
                    .buffer(self.vertices.getGpuBuffer().getBuffer());

            VkDevice device = setup.getLogicalDevice().getDevice();

            self.verticesDeviceAddress = VK14.vkGetBufferDeviceAddress(device, deviceAddressInfo);

            IndexBuffer ibo = new IndexBuffer(indices.length);
            ibo.put(indices);


            GpuBuffer.BufferUsage indexBufUsage = new GpuBuffer.BufferUsage(
                    GpuBuffer.BufferUsage.Bits.IBO,
                    GpuBuffer.BufferUsage.Bits.TRANSFER_DST
            );
            GpuBuffer.MemoryUsage indexMemUsage = new GpuBuffer.MemoryUsage(
                    GpuBuffer.MemoryUsage.Bits.GPU_ONLY
            );
            self.indices = new AllocatedBuffer(engine, setup, ibo, indexBufUsage, indexMemUsage);
            if (!VKUtils.setDebugName(renderer.getSetup().getLogicalDevice(), "IDX", self.indices.getGpuBuffer().getBuffer(), VK14.VK_OBJECT_TYPE_BUFFER)) {
                engine.throwException(new IllegalStateException("Couldn't set debug name"), "asd");
            }

            self.vertices.uploadViaStaging(engine, setup);
            self.indices.uploadViaStaging(engine, setup);

            return self;
        }
    }

    public AllocatedBuffer getVerticesBuf() {
        return vertices;
    }

    public AllocatedBuffer getIndicesBuf() {
        return indices;
    }

    public long verticesDeviceAddress() {
        return verticesDeviceAddress;
    }
}
