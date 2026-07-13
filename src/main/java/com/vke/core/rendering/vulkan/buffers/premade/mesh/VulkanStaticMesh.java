package com.vke.core.rendering.vulkan.buffers.premade.mesh;

import com.vke.api.rendering.abstraction.data.StaticMesh;
import com.vke.core.mesh.Mesh;
import com.vke.api.draw.Vertex;
import com.vke.core.services2.Services;
import com.vke.core.rendering.vulkan.service.VulkanRenderSystem;
import com.vke.core.rendering.vulkan.service.VulkanRenderer;
import com.vke.core.rendering.vulkan.buffers.StagedBuffer;
import com.vke.api.rendering.abstraction.enums.buffer.BufferUsage;
import com.vke.api.rendering.abstraction.enums.buffer.MemoryUsage;
import com.vke.core.rendering.vulkan.buffers.premade.ibo.IndexBuffer;
import com.vke.core.rendering.vulkan.buffers.premade.vbo.StaticVertexBuffer;
import com.vke.core.rendering.vulkan.command.VulkanCmdBuffers;
import com.vke.core.rendering.vulkan.device.VulkanRenderDevice;
import com.vke.utils.io.Disposable;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK14;
import org.lwjgl.vulkan.VkBufferDeviceAddressInfo;
import org.lwjgl.vulkan.VkDevice;

import java.util.Arrays;

public class VulkanStaticMesh implements Disposable, StaticMesh {
    private StagedBuffer vertices;
    private StagedBuffer indices;
    private long verticesDeviceAddress;

    private final VulkanRenderSystem sys;

    private VulkanStaticMesh(VulkanRenderSystem sys) {
        this.sys = sys;
    }

    public static <T extends Vertex> VulkanStaticMesh uploadOnce(VulkanRenderSystem sys, Mesh<T> mesh) {
        T[] vertices = mesh.getVertices();
        int[] indices = mesh.getIndices();

        VulkanRenderer renderer = sys.service(Services.VULKAN_RENDERER).assumeImplementation();
        if (vertices.length == 0) {
            sys.throwException(new IllegalStateException("Tried to upload empty buffer"), "MeshBuffer");
        }
        try(MemoryStack stack = MemoryStack.stackPush()) {
            VulkanRenderDevice d = renderer.getDevice();
            VulkanStaticMesh self = new VulkanStaticMesh(sys);

            T template = vertices[0];

            StaticVertexBuffer<T> vbo = new StaticVertexBuffer<>(sys, template, Arrays.asList(vertices));

            BufferUsage vertexBufUsage = new BufferUsage(
                    BufferUsage.Bits.SSBO,
                    BufferUsage.Bits.TRANSFER_DST,
                    BufferUsage.Bits.SHADER_DEVICE_ADDRESS,
                    BufferUsage.Bits.VBO
            );
            MemoryUsage vertexMemUsage = new MemoryUsage(
                    MemoryUsage.Bits.GPU_ONLY
            );
            self.vertices = new StagedBuffer(sys, vbo, vertexBufUsage, vertexMemUsage);

            VkBufferDeviceAddressInfo deviceAddressInfo = VkBufferDeviceAddressInfo.calloc(stack)
                    .sType$Default()
                    .buffer(self.vertices.getGpuBuffer().getBuffer());

            VkDevice device = sys.device().vkLogicalDevice();

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
            self.indices = new StagedBuffer(sys, ibo, indexBufUsage, indexMemUsage);

            self.vertices.uploadViaStaging(vbo::free);
            self.indices.uploadViaStaging(ibo::free);

            return self;
        }
    }

    @Override
    public void draw() {
        VulkanCmdBuffers cmd = sys.getCurrentCommandBuffer();
        bindIBO();
        bindVBO();
        cmd.drawIndexed(this.getIndexCount(), 1, 0, 0, 0);
    }

    @Override
    public void drawInstanced(int instanceCount) {
        bindIBO();
        bindVBO();
        sys.getCurrentCommandBuffer().drawIndexed(this.getIndexCount(), instanceCount, 0, 0, 0);
    }

    public void bindIBO() {
        VulkanCmdBuffers cmd = sys.getCurrentCommandBuffer();
        VK14.vkCmdBindIndexBuffer(cmd.getBuffer(), getIndicesBuf().getGpuBuffer().getBuffer(), 0, VK14.VK_INDEX_TYPE_UINT32);
    }

    public void bindVBO() {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            VulkanCmdBuffers cmd = sys.getCurrentCommandBuffer();
            VK14.vkCmdBindVertexBuffers(cmd.getBuffer(), 0, stack.longs(getVerticesBuf().getGpuBuffer().getBuffer()), stack.longs(0));
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
