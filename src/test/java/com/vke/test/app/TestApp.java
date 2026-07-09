package com.vke.test.app;

import com.vke.api.app.App;
import com.vke.api.assets.AssetHandle;
import com.vke.api.assets.r.R;
import com.vke.api.rendering.abstraction.pipeline.RenderPipeline;
import com.vke.api.rendering.vulkan.buffer.VertexByteSink;
import com.vke.api.rendering.vulkan.pushconstants.PushConstantHandle;
import com.vke.api.draw.Vertex;
import com.vke.core.VKEngine;
import com.vke.core.assets.service.AssetManagerScopedImpl;
import com.vke.core.rendering.draw.FrameContext;
import com.vke.core.vulkan.buffers.premade.mesh.StaticMeshBuffer;
import com.vke.core.services2.Services;
import com.vke.core.vulkan.command.VulkanCmdBuffers;
import com.vke.core.vulkan.pipeline.VulkanRenderPipeline;
import com.vke.core.vulkan.texture.VulkanTexture;
import com.vke.core.profiler.AppTimer;
import org.joml.Matrix4f;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK14;

import java.io.IOException;

public class TestApp extends App {
    private StaticMeshBuffer mesh;

    private AppTimer timer;

    PushConstantHandle vertexBufferPointer;

    PushConstantHandle projMatrixHandle;
    PushConstantHandle transformMatrixHandle;

    AssetHandle<RenderPipeline> CUBE = R.pipelines.get("spinny_cub.pipeline.json");

    @Override
    public void onInit(VKEngine engine) {
        AssetManagerScopedImpl assetManager = engine.service(Services.ASSET_MANAGER);
        assetManager.initAssets();

        CubeVertexFormat[] vf = new CubeVertexFormat[]{
                // Front (red)
                new CubeVertexFormat(-25, -25,  25, 1, 0, 0, 0.5f),
                new CubeVertexFormat( 25, -25,  25, 1, 0, 0, 0.5f),
                new CubeVertexFormat( 25,  25,  25, 1, 0, 0, 0.5f),
                new CubeVertexFormat(-25,  25,  25, 1, 0, 0, 0.5f),

                // Back (green)
                new CubeVertexFormat( 25, -25, -25, 0, 1, 0, 0.5f),
                new CubeVertexFormat(-25, -25, -25, 0, 1, 0, 0.5f),
                new CubeVertexFormat(-25,  25, -25, 0, 1, 0, 0.5f),
                new CubeVertexFormat( 25,  25, -25, 0, 1, 0, 0.5f),

                // Left (blue)
                new CubeVertexFormat(-25, -25, -25, 0, 0, 1, 0.5f),
                new CubeVertexFormat(-25, -25,  25, 0, 0, 1, 0.5f),
                new CubeVertexFormat(-25,  25,  25, 0, 0, 1, 0.5f),
                new CubeVertexFormat(-25,  25, -25, 0, 0, 1, 0.5f),

                // Right (yellow)
                new CubeVertexFormat( 25, -25,  25, 1, 1, 0, 0.5f),
                new CubeVertexFormat( 25, -25, -25, 1, 1, 0, 0.5f),
                new CubeVertexFormat( 25,  25, -25, 1, 1, 0, 0.5f),
                new CubeVertexFormat( 25,  25,  25, 1, 1, 0, 0.5f),

                // Top (magenta)
                new CubeVertexFormat(-25,  25,  25, 1, 0, 1, 0.5f),
                new CubeVertexFormat( 25,  25,  25, 1, 0, 1, 0.5f),
                new CubeVertexFormat( 25,  25, -25, 1, 0, 1, 0.5f),
                new CubeVertexFormat(-25,  25, -25, 1, 0, 1, 0.5f),

                // Bottom (cyan)
                new CubeVertexFormat(-25, -25, -25, 0, 1, 1, 0.5f),
                new CubeVertexFormat( 25, -25, -25, 0, 1, 1, 0.5f),
                new CubeVertexFormat( 25, -25,  25, 0, 1, 1, 0.5f),
                new CubeVertexFormat(-25, -25,  25, 0, 1, 1, 0.5f),
        };

        mesh = StaticMeshBuffer.uploadOnce(engine, vf, new int[]{
                // Front
                0, 1, 2,
                2, 3, 0,

                // Back
                4, 5, 6,
                6, 7, 4,

                // Left
                8, 9, 10,
                10, 11, 8,

                // Right
                12, 13, 14,
                14, 15, 12,

                // Top
                16, 17, 18,
                18, 19, 16,

                // Bottom
                20, 21, 22,
                22, 23, 20
        });

        timer = new AppTimer();

        VulkanRenderPipeline cubePipeline;
        try {
            pipeline = null; // (VulkanRenderPipeline) IDK.acquire(engine);
            cubePipeline = (VulkanRenderPipeline) CUBE.acquire(engine);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        //CombinedImageSamplerHandle sampl = pipeline.resolveUniform("tex");
        //sampl.set(scaryVk, Samplers.LINEAR);
        //pipeline.updateUniforms(sampl);

        vertexBufferPointer = pipeline.resolvePushConstant("vertexBuffer");
        matrixHandle = pipeline.resolvePushConstant("world");

        //cubeVertexBufferPointer = cubePipeline.resolvePushConstant("vertexBuffer");
        projMatrixHandle = cubePipeline.resolvePushConstant("world");
        transformMatrixHandle = cubePipeline.resolvePushConstant("translation");
    }

    PushConstantHandle vertexBufferPointer;
    PushConstantHandle matrixHandle;

    //PushConstantHandle cubeVertexBufferPointer;
    PushConstantHandle projMatrixHandle;
    PushConstantHandle transformMatrixHandle;

    //AssetHandle<RenderPipeline> IDK = R.pipelines.get("test.pipeline.json");
    AssetHandle<RenderPipeline> CUBE = R.pipelines.get("spinny_cub.pipeline_vt.json");

    @Override
    public void onDraw(FrameContext ctx) {
        timer.onFrameStart();

        try (MemoryStack stack = MemoryStack.stackPush()) {
            VulkanCmdBuffers cmd = (VulkanCmdBuffers) ctx.getCommandBuffer();
            //cmd.bindPipeline(IDK);

            Matrix4f mat = new Matrix4f();
            mat.setOrtho(0, 800, 0, 600, 0, 1000, true);

            vertexBufferPointer.write(buf -> buf.putLong(mesh.verticesDeviceAddress()));
            projMatrixHandle.write(buf -> buf.putMat4(mat));


            // Set sampler (outside of render loop tho)

            //cmd.bindDescriptorSets(IDK);

            //cmd.setPushConstants(IDK);

            VK14.vkCmdBindIndexBuffer(cmd.getBuffer(), mesh.getIndicesBuf().getGpuBuffer().getBuffer(), 0, VK14.VK_INDEX_TYPE_UINT32);

            VK14.vkCmdDrawIndexed(cmd.getBuffer(), mesh.getIndexCount(), 1, 0, 0, 0);

            // 2nd draw:
            cmd.bindPipeline(CUBE);


            Matrix4f model = new Matrix4f();

            // time in seconds (you need to supply this)
            float time = (System.nanoTime() / 1_000_000_000.0f);

            // rotation speed (radians per second)
            float speed = 1.0f;

            // build transform
            model.identity()
                    .translate(400.0f, 300.0f, -50) // move to center (adjust as needed)
                    .scale((float) (5 + 5 * Math.sin(Math.toRadians(time * 10))),
                            (float) (5 + 5 * Math.sin(Math.toRadians(time * 10))),
                            (float) (5 + 5 * Math.sin(Math.toRadians(time * 10))))
                    .rotateXYZ(time * speed, time * speed, time * speed);

            projMatrixHandle.write(buf -> buf.putMat4(mat));
            transformMatrixHandle.write(buf -> buf.putMat4(model));

            cmd.bindDescriptorSets(CUBE);
            cmd.setPushConstants(CUBE);

            VK14.vkCmdBindIndexBuffer(cmd.getBuffer(), mesh.getIndicesBuf().getGpuBuffer().getBuffer(), 0, VK14.VK_INDEX_TYPE_UINT32);
            VK14.vkCmdBindVertexBuffers(cmd.getBuffer(), 0, stack.longs(mesh.getVerticesBuf().getGpuBuffer().getBuffer()), stack.longs(0));

            VK14.vkCmdDrawIndexed(cmd.getBuffer(), mesh.getIndexCount(), 1, 0, 0, 0);
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
        mesh.free();
    }

    private static class CubeVertexFormat extends Vertex {
        private final float x, y, z;
        private final float r, g, b, a;

        public CubeVertexFormat(float x, float y, float z, float r, float g, float b, float a) {
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
        public void putSelf(VertexByteSink buf) {
            //AlignedByteBuffer abb = new AlignedByteBuffer(buf, 16);
            //abb.float3(x, y, z);
            //abb.float4(r, g, b, a);

            buf.float3(x, y, z);
            buf.float4(r, g, b, a);

//            buf.putFloat(x);
//            buf.putFloat(y);
//            buf.putFloat(z);
//
//            buf.putFloat(r);
//            buf.putFloat(g);
//            buf.putFloat(b);
//            buf.putFloat(a);
        }
    }

}
