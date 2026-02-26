package com.vke.test.app;

import com.vke.api.app.App;
import com.vke.api.utils.AlignedByteBuffer;
import com.vke.api.rendering.vulkan.buffer.Vertex;
import com.vke.core.VKEngine;
import com.vke.core.assets.VKEAssetManager;
import com.vke.core.vulkan.buffers.premade.MeshBuffer;
import com.vke.core.vulkan.Scissor;
import com.vke.core.vulkan.Viewport;
import com.vke.core.services.Services;
import com.vke.core.vulkan.VulkanRenderer;
import com.vke.core.vulkan.command.VulkanCmdBuffers;
import com.vke.core.vulkan.sampler.Samplers;
import com.vke.core.vulkan.texture.VulkanTexture;
import com.vke.core.window.Window;
import com.vke.utils.AppTimer;
import org.joml.Matrix4f;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK14;

import java.io.IOException;
import java.nio.ByteBuffer;

public class TestApp extends App {
    private MeshBuffer mesh;
    private MeshBuffer mesh2;

    private AppTimer timer;
    private VulkanTexture scaryVk;

    @Override
    public void onInit(VKEngine engine) {
        VertexFormatTexture[] vertices = new VertexFormatTexture[]{
                //new VertexFormat(-1,  1, 0, 1, 0, 0, 1),
                //new VertexFormat( 1,  1, 0, 0, 1, 0, 1),
                //new VertexFormat(-1, -1, 0, 0, 0, 1, 1),
                //new VertexFormat( 1, -1, 0, 1, 1, 0, 1)

                new VertexFormatTexture(0, 0, -50, 1, 0, 0, 1f, 0, 0),
                new VertexFormatTexture(255, 0, -50, 0, 1, 0, 1f, 1, 0),
                new VertexFormatTexture(255, 255, -50, 0, 0, 1, 1f, 1, 1),
                new VertexFormatTexture(0, 255, -50, 1, 1, 0, 1f, 0, 1)

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

        TestPipelines.STH.setUniform("fColor", (slice) -> {
            slice.write((buf) -> {
                buf.putFloat(1);
                buf.putFloat(0);
                buf.putFloat(0);
                buf.putFloat(1);
            });
        });

        TestPipelines.STH.setUniform("sColor", (slice) -> {
            slice.write((buf) -> {
                buf.putFloat(0);
                buf.putFloat(0);
                buf.putFloat(1);
                buf.putFloat(1);
            });
        });

        timer = new AppTimer();

        VulkanRenderer renderer = engine.service(Services.VULKAN_RENDERER);
        VKEAssetManager R = engine.service(Services.ASSET_MANAGER);
        R.swapBundle(engine.id("scene1"));

        try {
            scaryVk = (VulkanTexture) R.getAsset(engine.id("scaryvulkan.png")).acquire(engine);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        //scaryVk = renderer.getDevice().createTexture(new Identifier("scaryvulkan.png"), Texture.TextureDesc.albedo2D(1920, 1080));
        //VKUtils.setDebugName(renderer.getDevice().getLogicalDevice(), "SCARY_VULKAN", scaryVk.getHandle(), VK14.VK_OBJECT_TYPE_IMAGE);

        TestPipelines.IDK.setSampler("tex", Samplers.LINEAR, scaryVk);
    }

    @Override
    public void onDraw(Window window, VulkanRenderer.FrameData fd) {
        timer.onFrameStart();

        int width = window.getSize().width();
        int height = window.getSize().height();

        try (MemoryStack stack = MemoryStack.stackPush()) {
            VulkanCmdBuffers cmd = fd.frame().getBuffers();
            //cmd.bindRenderPipeline(TestPipelines.IDK);

            Scissor sc = new Scissor(0, 0, width, height);
            Viewport wp = new Viewport(0, 0, width, height);

            cmd.setViewport(wp);
            cmd.setScissor(sc);

            Matrix4f mat = new Matrix4f();
            mat.setOrtho(0, wp.width(), 0, wp.height(), 0, 100, true);

            TestPushConstant pc = TestPipelines.IDK.getPushConstant("vertexBufferPtr");
            pc.setVerticesPtr(mesh.verticesDeviceAddress());
            pc.setMat(mat);

            // Set sampler (outside of render loop tho)

            //cmd.bindDescriptorSets(TestPipelines.IDK);

            //cmd.setPushConstants(TestPipelines.IDK);

            VK14.vkCmdBindIndexBuffer(cmd.getBuffer(), mesh.getIndicesBuf().getGpuBuffer().getBuffer(), 0, VK14.VK_INDEX_TYPE_UINT32);

            VK14.vkCmdDrawIndexed(cmd.getBuffer(), mesh.getIndexCount(), 1, 0, 0, 0);


            // 2nd draw:
            //cmd.bindRenderPipeline(TestPipelines.STH);

            ((SthPushConstant) TestPipelines.STH.getPushConstant("vertexBufferPtr")).setVerticesPtr(mesh2.verticesDeviceAddress());

            TestPipelines.STH.setUniform("time", (slice) -> {
                slice.write((buf) -> {
                    buf.putFloat(System.currentTimeMillis() % 1000);
                });
            });

            //cmd.bindDescriptorSets(TestPipelines.STH);

            //cmd.setPushConstants(TestPipelines.STH);

            VK14.vkCmdBindIndexBuffer(cmd.getBuffer(), mesh2.getIndicesBuf().getGpuBuffer().getBuffer(), 0, VK14.VK_INDEX_TYPE_UINT32);

            VK14.vkCmdDrawIndexed(cmd.getBuffer(), mesh2.getIndexCount(), 1, 0, 0, 0);
        }

        if (timer.onFrameComplete(AppTimer.DEFAULT_TEST_INTERVAL_BEING_THE_DURATION_OF_9192631770_PERIODS_OF_THE_RADIATION_CORRESPONDING_TO_THE_TRANSITION_BETWEEN_THE_TWO_HYPERFINE_LEVELS_OF_THE_GROUND_STATE_OF_THE_CAESIUM_133_ATOM_EXPRESSED_IN_MILLISECONDS)) {
            System.out.println("FPS: " + timer.fps());
        }
    }

    @Override
    public String getName() {
        return "TestApp";
    }

    @Override
    public void free() {
        //scaryVk.free();
        mesh.free();
        mesh2.free();
    }

    private static class VertexFormatTexture implements Vertex {
        private final float x, y, z;
        private final float r, g, b, a;
        private final float u, v;

        public VertexFormatTexture(float x, float y, float z, float r, float g, float b, float a, float u, float v) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.r = r;
            this.g = g;
            this.b = b;
            this.a = a;
            this.u = u;
            this.v = v;
        }

        @Override
        public int getByteStride() {
            return Float.BYTES * 12;
        }

        @Override
        public void putSelf(ByteBuffer buf) {
            AlignedByteBuffer abb = new AlignedByteBuffer(buf, 16);
            abb.float3(x, y, z);
            abb.float4(r, g, b, a);
            abb.float2(u, v);
        }

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
