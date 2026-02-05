package com.vke.test;

import com.vke.api.game.Game;
import com.vke.api.vulkan.buffer.Vertex;
import com.vke.core.VKEngine;
import com.vke.core.rendering.buffer.MeshBuffer;
import com.vke.core.rendering.vulkan.Scissor;
import com.vke.core.rendering.vulkan.Viewport;
import com.vke.core.rendering.vulkan.VulkanRenderer;
import com.vke.core.rendering.vulkan.commands.CommandBuffers;
import com.vke.core.rendering.vulkan.pipeline.RenderPipelines;
import com.vke.core.services.Services;
import com.vke.core.window.Window;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK14;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class TestApp extends Game {
    private MeshBuffer<VertexFormat> mesh;

    @Override
    public void onInit(VKEngine engine) {
        VertexFormat[] vertices = new VertexFormat[]{
                new VertexFormat(-1, -1, 0, 0, 1, 1, 1),
                new VertexFormat(0, 1, 0, 1, 0, 1, 1),
                new VertexFormat(1, -1, 0, 1, 1, 0, 1)

                /*
                  1
                 / \
                0 - 2
                 */
        };
        mesh = MeshBuffer.uploadOnce(engine, engine.service(Services.VULKAN_RENDERER), vertices, new int[]{0, 1, 2});
    }

    @Override
    public void onDraw(Window window, VulkanRenderer.FrameData fd) {
        CommandBuffers cmd = fd.cmd();
        MemoryStack stack = fd.getStack();
        cmd.bindRenderPipeline(RenderPipelines.IDK);

        new Scissor().use(fd);
        new Viewport().use(fd);

        TestPushConstant pc = RenderPipelines.IDK.getPushConstant("vertexBufferPtr");
        pc.setVerticesPtr(mesh.verticesDeviceAddress());

        cmd.setPushConstants(RenderPipelines.IDK, stack);

        VK14.vkCmdBindIndexBuffer(cmd.getBuffer(), mesh.getIndicesBuf().getGpuBuffer().getBuffer(), 0, VK14.VK_INDEX_TYPE_UINT32);

        VK14.vkCmdDrawIndexed(cmd.getBuffer(), 3, 1, 0, 0, 0);
        //VK14.vkCmdDraw(cmd.getBuffer(), 3, 1, 0, 0);
    }

    private static class VertexFormat implements Vertex {
        private final float x, y, z;
        private final float r, g, b, a;

        public VertexFormat(float x, float y, float z, float r, float g, float b, float a) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.r = r;
            this.g = g;
            this.b = b;
            this.a = a;
        }

        @Override
        public int getByteStride() {
            return Float.BYTES * 7;
        }

        @Override
        public void putSelf(ByteBuffer buf) {
            buf.putFloat(x);
            buf.putFloat(y);
            buf.putFloat(z);

            buf.putFloat(r);
            buf.putFloat(g);
            buf.putFloat(b);
            buf.putFloat(a);
        }
    }
}
