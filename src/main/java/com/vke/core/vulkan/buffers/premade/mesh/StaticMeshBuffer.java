package com.vke.core.vulkan.buffers.premade.mesh;

import com.vke.core.mesh.Mesh;
import com.vke.api.draw.Vertex;
import com.vke.core.VKEngine;
import com.vke.core.vulkan.VulkanRenderer;
import com.vke.core.vulkan.buffers.StagedBuffer;
import com.vke.api.rendering.abstraction.enums.buffer.BufferUsage;
import com.vke.api.rendering.abstraction.enums.buffer.MemoryUsage;
import com.vke.core.vulkan.buffers.premade.ibo.IndexBuffer;
import com.vke.core.vulkan.buffers.premade.vbo.StaticVertexBuffer;
import com.vke.core.vulkan.device.VulkanRenderDevice;
import com.vke.utils.io.Disposable;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK14;
import org.lwjgl.vulkan.VkBufferDeviceAddressInfo;
import org.lwjgl.vulkan.VkDevice;

import java.util.Arrays;

public class StaticMeshBuffer implements Disposable {
    private StagedBuffer vertices;
    private StagedBuffer indices;
    private long verticesDeviceAddress;

    private StaticMeshBuffer() {}

    public static StaticMeshBuffer uploadOnce(VKEngine engine, VulkanRenderer renderer, Mesh mesh) {
        return StaticMeshBuffer.uploadOnce(engine, renderer, mesh, false);
    }

    public static <T extends Vertex> StaticMeshBuffer uploadOnce(VKEngine engine, VulkanRenderer renderer, T[] vertices, int[] indices) {
        return StaticMeshBuffer.uploadOnce(engine, renderer, vertices, indices, false);
    }

    public static StaticMeshBuffer uploadOnce(VKEngine engine, VulkanRenderer renderer, Mesh mesh, boolean align16) {
        return uploadOnce(engine, renderer, mesh.getVertices(), mesh.getIndices(), align16);
    }

    public static <T extends Vertex> StaticMeshBuffer uploadOnce(VKEngine engine, VulkanRenderer renderer, T[] vertices, int[] indices, boolean align16) {
        if (vertices.length == 0) {
            engine.throwException(new IllegalStateException("Tried to upload empty buffer"), "MeshBuffer");
        }
        try(MemoryStack stack = MemoryStack.stackPush()) {
            VulkanRenderDevice d = renderer.getDevice();
            StaticMeshBuffer self = new StaticMeshBuffer();

            T template = vertices[0];

            StaticVertexBuffer<T> vbo = new StaticVertexBuffer<>(template, Arrays.asList(vertices), align16);

            BufferUsage vertexBufUsage = new BufferUsage(
                    BufferUsage.Bits.SSBO,
                    BufferUsage.Bits.TRANSFER_DST,
                    BufferUsage.Bits.SHADER_DEVICE_ADDRESS,
                    BufferUsage.Bits.VBO
            );
            MemoryUsage vertexMemUsage = new MemoryUsage(
                    MemoryUsage.Bits.GPU_ONLY
            );
            self.vertices = new StagedBuffer(engine, d, vbo, vertexBufUsage, vertexMemUsage);

            VkBufferDeviceAddressInfo deviceAddressInfo = VkBufferDeviceAddressInfo.calloc(stack)
                    .sType$Default()
                    .buffer(self.vertices.getGpuBuffer().getBuffer());

            VkDevice device = renderer.getDevice().getLogicalDevice().getDevice();

            self.verticesDeviceAddress = VK14.vkGetBufferDeviceAddress(device, deviceAddressInfo);

            IndexBuffer ibo = new IndexBuffer(indices.length);
            ibo.put(indices);


            BufferUsage indexBufUsage = new BufferUsage(
                    BufferUsage.Bits.IBO,
                    BufferUsage.Bits.TRANSFER_DST
            );
            MemoryUsage indexMemUsage = new MemoryUsage(
                    MemoryUsage.Bits.GPU_ONLY
            );
            self.indices = new StagedBuffer(engine, d, ibo, indexBufUsage, indexMemUsage);

            self.vertices.uploadViaStaging(engine, d, vbo::free);
            self.indices.uploadViaStaging(engine, d, ibo::free);

            return self;
        }
    }

    public StagedBuffer getVerticesBuf() {
        return vertices;
    }

    public StagedBuffer getIndicesBuf() {
        return indices;
    }

    public long verticesDeviceAddress() {
        return verticesDeviceAddress;
    }

    public int getIndexCount() {
        return indices.getCpuBuffer().elementCount;
    }

    @Override
    public void free() {
        this.vertices.free();
        this.indices.free();
    }
}
