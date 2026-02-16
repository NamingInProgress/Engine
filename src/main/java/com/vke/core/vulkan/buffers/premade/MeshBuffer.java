package com.vke.core.vulkan.buffers.premade;

import com.vke.api.vulkan.buffer.Vertex;
import com.vke.core.VKEngine;
import com.vke.core.vulkan.VKUtils;
import com.vke.core.vulkan.VulkanRenderer;
import com.vke.core.vulkan.buffers.StagedBuffer;
import com.vke.api.abstraction.descriptors.buffer.BufferUsage;
import com.vke.api.abstraction.descriptors.buffer.MemoryUsage;
import com.vke.core.vulkan.device.VulkanRenderDevice;
import com.vke.utils.Disposable;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK14;
import org.lwjgl.vulkan.VkBufferDeviceAddressInfo;
import org.lwjgl.vulkan.VkDevice;

import java.util.Arrays;

public class MeshBuffer implements Disposable {
    private StagedBuffer vertices;
    private StagedBuffer indices;
    private long verticesDeviceAddress;

    private MeshBuffer() {}

    public static <T extends Vertex> MeshBuffer uploadOnce(VKEngine engine, VulkanRenderer renderer, T[] vertices, int[] indices) {
        if (vertices.length == 0) {
            engine.throwException(new IllegalStateException("Tried to upload empty buffer"), "MeshBuffer");
        }
        try(MemoryStack stack = MemoryStack.stackPush()) {
            VulkanRenderDevice d = renderer.getDevice();
            MeshBuffer self = new MeshBuffer();

            T template = vertices[0];

            StaticVertexBuffer<T> vbo = new StaticVertexBuffer<>(template, Arrays.asList(vertices));

            BufferUsage vertexBufUsage = new BufferUsage(
                    BufferUsage.Bits.SSBO,
                    BufferUsage.Bits.TRANSFER_DST,
                    BufferUsage.Bits.SHADER_DEVICE_ADDRESS
            );
            MemoryUsage vertexMemUsage = new MemoryUsage(
                    MemoryUsage.Bits.GPU_ONLY
            );
            self.vertices = new StagedBuffer(engine, d, vbo, vertexBufUsage, vertexMemUsage);
            if (!VKUtils.setDebugName(d.getLogicalDevice(), "Verts", self.vertices.getGpuBuffer().getBuffer(), VK14.VK_OBJECT_TYPE_BUFFER)) {
                engine.throwException(new IllegalStateException("Couldn't set debug name"), "asd");
            }

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
            if (!VKUtils.setDebugName(d.getLogicalDevice(), "IDX", self.indices.getGpuBuffer().getBuffer(), VK14.VK_OBJECT_TYPE_BUFFER)) {
                engine.throwException(new IllegalStateException("Couldn't set debug name"), "asd");
            }

            self.vertices.uploadViaStaging(engine, d);
            self.indices.uploadViaStaging(engine, d);

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
