package com.vke.test.app;

import com.vke.api.app.App;
import com.vke.api.utils.AlignedByteBuffer;
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
import com.vke.test.TestPipelines;
import org.joml.Matrix4f;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK14;

import java.nio.ByteBuffer;

public class TestApp extends App {
    private MeshBuffer mesh;
    private MeshBuffer mesh2;

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

        VertexFormat[] verts = new VertexFormat[]{
                new VertexFormat(-0.5f, -0.5f, 0.5f, 1, 0, 0, 0.5f),
                new VertexFormat(0.5f, -0.5f, 0.5f, 1, 0, 0, 0.5f),
                new VertexFormat(0, 0.5f, 0.5f, 1, 0, 0, 0.5f),

                new VertexFormat(-0.5f, 0.5f, 0.5f, 0, 0, 1, 0.5f),
                new VertexFormat(0.5f, 0.5f, 0.5f, 0, 0, 1, 0.5f),
                new VertexFormat(0f, -0.5f, 0.5f, 0, 0, 1, 0.5f)
        };

        mesh = MeshBuffer.uploadOnce(engine, engine.service(Services.VULKAN_RENDERER), vertices, new int[]{0, 1, 2, 2, 3, 0});
        mesh2 = MeshBuffer.uploadOnce(engine, engine.service(Services.VULKAN_RENDERER), verts, new int[]{0, 1, 2, 3, 4, 5});

        TestPipelines.IDK.setDescriptorEntryData("customColor", (slice) -> {
            slice.write((buf) -> {
                buf.putFloat(1);
                buf.putFloat(0);
                buf.putFloat(0);
                buf.putFloat(1);
            });
        });

        TestPipelines.STH.setDescriptorEntryData("fColor", (slice) -> {
            slice.write((buf) -> {
                buf.putFloat(1);
                buf.putFloat(0);
                buf.putFloat(0);
                buf.putFloat(1);
            });
        });

        TestPipelines.STH.setDescriptorEntryData("sColor", (slice) -> {
            slice.write((buf) -> {
                buf.putFloat(0);
                buf.putFloat(0);
                buf.putFloat(1);
                buf.putFloat(1);
            });
        });
    }

    @Override
    public void onDraw(Window window, VulkanRenderer.FrameData fd) {
        CommandBuffers cmd = fd.cmd();
        MemoryStack stack = fd.getStack();
        cmd.bindRenderPipeline(TestPipelines.IDK);

        new Scissor().use(fd);
        Viewport wp = new Viewport().use(fd);
        //System.out.println("wp.width() = " + wp.width());
        //System.out.println("wp.height() = " + wp.height());

        Matrix4f mat = new Matrix4f();
        mat.setOrtho(0, wp.width(), 0, wp.height(), 0, 100, true);

        TestPushConstant pc = TestPipelines.IDK.getPushConstant("vertexBufferPtr");
        pc.setVerticesPtr(mesh.verticesDeviceAddress());
        pc.setMat(mat);

        Matrix4f translation = new Matrix4f();
        translation.translation((float) (Math.abs(Math.sin(System.currentTimeMillis()) * 20f - 1f)), 0, 0);
        TestPipelines.IDK.setDescriptorEntryData("matrix", (slice) -> {
            slice.write((buf) -> {
                AlignedByteBuffer abb = new AlignedByteBuffer(buf, 16);
                abb.float4x4(translation);
            });
        });

        TestPipelines.IDK.setDescriptorEntryData("time", (slice) -> {
            slice.write((buf) -> {
                buf.putFloat(System.currentTimeMillis() % 1000);
            });
        });

        cmd.setDescriptorSets(TestPipelines.IDK, stack);

        cmd.setPushConstants(TestPipelines.IDK, stack);

        VK14.vkCmdBindIndexBuffer(cmd.getBuffer(), mesh.getIndicesBuf().getGpuBuffer().getBuffer(), 0, VK14.VK_INDEX_TYPE_UINT32);

        VK14.vkCmdDrawIndexed(cmd.getBuffer(), mesh.getIndexCount(), 1, 0, 0, 0);


        // 2nd draw:
        cmd.bindRenderPipeline(TestPipelines.STH);

        ((SthPushConstant) TestPipelines.STH.getPushConstant("vertexBufferPtr")).setVerticesPtr(mesh2.verticesDeviceAddress());

        TestPipelines.STH.setDescriptorEntryData("time", (slice) -> {
            slice.write((buf) -> {
                buf.putFloat(System.currentTimeMillis() % 1000);
            });
        });

        cmd.setDescriptorSets(TestPipelines.STH, stack);

        cmd.setPushConstants(TestPipelines.STH, stack);

        VK14.vkCmdBindIndexBuffer(cmd.getBuffer(), mesh2.getIndicesBuf().getGpuBuffer().getBuffer(), 0, VK14.VK_INDEX_TYPE_UINT32);

        VK14.vkCmdDrawIndexed(cmd.getBuffer(), mesh2.getIndexCount(), 1, 0, 0, 0);


        //VK14.vkCmdDraw(cmd.getBuffer(), 3, 1, 0, 0);

        //cmd.bindRenderPipeline(RenderPipelines.MAIN);
        //cmd.setPushConstants(RenderPipelines.MAIN, stack);
        //VK14.vkCmdDraw(cmd.getBuffer(), 3, 1, 0, 0);
    }

    @Override
    public void free() {
        mesh.free();
        mesh2.free();
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
