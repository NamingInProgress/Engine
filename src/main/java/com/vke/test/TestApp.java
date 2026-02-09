package com.vke.test;

import com.vke.api.app.App;
import com.vke.api.utils.AlignedByteBuffer;
import com.vke.api.vulkan.buffer.Vertex;
import com.vke.api.vulkan.pipeline.RenderPipeline;
import com.vke.core.VKEngine;
import com.vke.core.rendering.buffer.MeshBuffer;
import com.vke.core.rendering.vulkan.Scissor;
import com.vke.core.rendering.vulkan.Viewport;
import com.vke.core.rendering.vulkan.VulkanRenderer;
import com.vke.core.rendering.vulkan.commands.CommandBuffers;
import com.vke.core.rendering.vulkan.pipeline.RenderPipelines;
import com.vke.core.services.Services;
import com.vke.core.window.Window;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK14;

import java.nio.ByteBuffer;

public class TestApp extends App {
    private MeshBuffer mesh;

    @Override
    public void onInit(VKEngine engine) {
        VertexFormat[] vertices = new VertexFormat[]{
                //new VertexFormat(-1,  1, 0, 1, 0, 0, 1),
                //new VertexFormat( 1,  1, 0, 0, 1, 0, 1),
                //new VertexFormat(-1, -1, 0, 0, 0, 1, 1),
                //new VertexFormat( 1, -1, 0, 1, 1, 0, 1)

                new VertexFormat(0, 0, -50, 1, 0, 0, 1f),
                new VertexFormat(255, 0, -50, 0, 1, 0, 1f),
                new VertexFormat(255, 255, -50, 0, 0, 1, 1f),
                new VertexFormat(0, 255, -50, 1, 1, 0, 1f)

                //new VertexFormat(-0.5f, -0.5f, 0.5f, 1, 0, 0, 0.5f),
                //new VertexFormat(0.5f, -0.5f, 0.5f, 1, 0, 0, 0.5f),
                //new VertexFormat(0, 0.5f, 0.5f, 1, 0, 0, 0.5f),

                //new VertexFormat(-0.5f, 0.5f, 0.5f, 0, 0, 1, 0.5f),
                //new VertexFormat(0.5f, 0.5f, 0.5f, 0, 0, 1, 0.5f),
                //new VertexFormat(0f, -0.5f, 0.5f, 0, 0, 1, 0.5f)

                /*
                  1    3 - 2
                 / \   | / |
                0 - 2  0 - 1
                 */
        };
        mesh = MeshBuffer.uploadOnce(engine, engine.service(Services.VULKAN_RENDERER), vertices, new int[]{0, 1, 2, 2, 3, 0});
    }

    @Override
    public void onDraw(Window window, VulkanRenderer.FrameData fd) {
        CommandBuffers cmd = fd.cmd();
        MemoryStack stack = fd.getStack();
        cmd.bindRenderPipeline(RenderPipelines.IDK);

        new Scissor().use(fd);
        Viewport wp = new Viewport().use(fd);
        //System.out.println("wp.width() = " + wp.width());
        //System.out.println("wp.height() = " + wp.height());

        Matrix4f mat = new Matrix4f();
        mat.setOrtho(0, wp.width(), 0, wp.height(), 0, 100, true);

        TestPushConstant pc = RenderPipelines.IDK.getPushConstant("vertexBufferPtr");
        pc.setVerticesPtr(mesh.verticesDeviceAddress());
        pc.setMat(mat);

        RenderPipelines.IDK.setDescriptorEntryData("viewProj", (slice) -> {
            slice.put(myData);
        });

        cmd.setPushConstants(RenderPipelines.IDK, stack);

        VK14.vkCmdBindIndexBuffer(cmd.getBuffer(), mesh.getIndicesBuf().getGpuBuffer().getBuffer(), 0, VK14.VK_INDEX_TYPE_UINT32);

        VK14.vkCmdDrawIndexed(cmd.getBuffer(), mesh.getIndexCount(), 1, 0, 0, 0);
        //VK14.vkCmdDraw(cmd.getBuffer(), 3, 1, 0, 0);

        //cmd.bindRenderPipeline(RenderPipelines.MAIN);
        //cmd.setPushConstants(RenderPipelines.MAIN, stack);
        //VK14.vkCmdDraw(cmd.getBuffer(), 3, 1, 0, 0);
    }

    @Override
    public void free() {
        mesh.free();
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
            return Float.BYTES * 8;
        }

        @Override
        public void putSelf(ByteBuffer buf) {
            AlignedByteBuffer abb = new AlignedByteBuffer(buf, 16);
            abb.float3(x, y, z);
            abb.float4(r, g, b, a);
        }
    }
}
