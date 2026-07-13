package com.vke.test.rendering;

import com.vke.api.assets.r.R;
import com.vke.api.draw.VertexConsumer;
import com.vke.core.mesh.MeshPrefab;
import com.vke.api.draw.Vertex;
import com.vke.api.rendering.abstraction.pipeline.RenderPipeline;
import com.vke.api.rendering.vulkan.buffer.VertexEcoder;
import com.vke.api.rendering.vulkan.pushconstants.PushConstantHandle;
import com.vke.api.scene.Scene;
import com.vke.core.Context;
import com.vke.core.assets.handles.LazyAssetHandle;
import com.vke.core.profiler.AppTimer;
import com.vke.core.rendering.draw.FrameContext;
import com.vke.core.services2.Services;
import com.vke.core.vulkan.service.VulkanRenderer;
import com.vke.core.vulkan.buffers.premade.mesh.StaticMeshBuffer;
import com.vke.core.vulkan.command.VulkanCmdBuffers;
import com.vke.core.vulkan.pipeline.VulkanComputePipeline;
import com.vke.core.vulkan.pipeline.VulkanRenderPipeline;
import com.vke.core.vulkan.swapchain.VulkanSwapchain;
import com.vke.core.rendering.vertexconsumer.FastVertexConsumer;
import com.vke.core.vulkan.texture.texture2.VulkanTexture;
import com.vke.utils.io.Identifier;
import org.joml.Matrix4f;
import org.lwjgl.system.MemoryStack;

import java.io.IOException;

public class MainScene extends Scene {

    public MainScene(Identifier name, Context context) {
        super(name, context);
    }

    private VulkanRenderPipeline cubePipeline, dynamicVertsPipeline, fullScreenPipeline;
    private VulkanComputePipeline computePipeline;
    private LazyAssetHandle<RenderPipeline> CUBE = R.pipelines.get("spinny_cub.pipeline_vt.json");
    private LazyAssetHandle<RenderPipeline> DYNAMIC = R.pipelines.get("dynamic_vertices_test.pipeline.json");
    //private LazyAssetHandle<ComputePipeline> COMPUTE = R.compute_pipelines.get("compute_pipeline.compute_pipeline.json");
    //private LazyAssetHandle<RenderPipeline> QUAD = R.pipelines.get("fullscreen_quad.pipeline.json");


    private PushConstantHandle projMatrixHandle;
    private PushConstantHandle transformMatrixHandle;

    private PushConstantHandle dvProjMatrixHandle;
    private PushConstantHandle dvTransformMatrixHandle;


    private VertexConsumer<DynamicTestVertex> consumer;

    private VulkanTexture tex;

    private VulkanSwapchain sw;
    private VulkanRenderer renderer;

    private StaticMeshBuffer mesh;
    private MeshPrefab prefab;

    private final AppTimer timer = new AppTimer();

    @Override
    public void onLoad() {
        renderer = context.service(Services.VULKAN_RENDERER);
        var device = renderer.getDevice();

        cubePipeline = (VulkanRenderPipeline) CUBE.assume(context);
        dynamicVertsPipeline = (VulkanRenderPipeline) DYNAMIC.assume(context);
        //computePipeline = (VulkanComputePipeline) COMPUTE.assume(context);
        //fullScreenPipeline = (VulkanRenderPipeline) QUAD.assume(context);
        
        projMatrixHandle = cubePipeline.resolvePushConstant("world");
        transformMatrixHandle = cubePipeline.resolvePushConstant("translation");

        dvProjMatrixHandle = dynamicVertsPipeline.resolvePushConstant("world");
        dvTransformMatrixHandle = dynamicVertsPipeline.resolvePushConstant("translation");


        //sw = renderer.swapchain;


        //compute_image = computePipeline.resolveUniform("image");
        //compute_image.set(tex);

        //fullScreenSampler = fullScreenPipeline.resolveUniform("image");
        //fullScreenSampler.set(R.textures.get("scaryvulkan.png").assume(context), Samplers.LINEAR);

        consumer = new FastVertexConsumer<>(context.getEngine(), context.service(Services.VULKAN_RENDERER), new DynamicTestVertex(0, 0, 0, 0, 0, 0, 0));

        try {
            prefab = R.meshprefabs.get("bear.obj").acquire(context);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        float[] color = {1, 1, 1, 1};

        mesh = StaticMeshBuffer.uploadOnce(context.getEngine(),
                prefab.toMesh((prefabVertex -> new CubeVertexFormat(
                        prefabVertex.position()[0],
                        prefabVertex.position()[1],
                        prefabVertex.position()[2],

                        prefabVertex.normal()[0],
                        prefabVertex.normal()[1],
                        prefabVertex.normal()[2],

                        color[0],
                        color[1],
                        color[2],
                        color[3]))));
    }

    @Override
    public void onDraw(FrameContext ctx) {
        timer.onFrameStart();
        try (MemoryStack stack = MemoryStack.stackPush()) {
            VulkanCmdBuffers cmd = (VulkanCmdBuffers) ctx.getCommandBuffer();
            consumer.beginFrame();
            cmd.bindPipeline(CUBE);
            cmd.bindPipeline(CUBE);

            Matrix4f mat = new Matrix4f();
            //mat.setOrtho(0, wp.width(), 0, wp.height(), 0, 1000, true);
            mat.setPerspective((float) Math.toRadians(90), (float) 800 / 600, 0.1f, 1000, true);

            Matrix4f model = new Matrix4f();

            float time = (System.nanoTime() / 1_000_000_000.0f);

            float speed = 1.0f;

            float scale = 10;
            model.identity()
                    .translate(200.0f, -250.0f, -550)
                    .scale(scale, scale, scale)
                    .rotateY(time * speed);

            projMatrixHandle.write(buf -> buf.putMat4(mat));
            transformMatrixHandle.write(buf -> buf.putMat4(model));

            cmd.setPushConstants(CUBE);

            mesh.draw(ctx);


            cmd.bindPipeline(DYNAMIC);

            dvProjMatrixHandle.write(buf -> buf.putMat4(mat));
            dvTransformMatrixHandle.write(buf -> buf.putMat4(new Matrix4f().translate(-220, -250.0f, -550).scale(scale, scale, scale).rotateY(time * speed)));

            cmd.setPushConstants(DYNAMIC);

            consumer.begin();

            consumer.mesh(prefab.toMesh((prefabVertex) -> new DynamicTestVertex(
                    prefabVertex.position()[0],
                    prefabVertex.position()[1],
                    prefabVertex.position()[2],

                    1,
                    1,
                    1,
                    1
            )));

            consumer.begin();
            consumer.vertices(new DynamicTestVertex(0, 0, 0, 1, 0, 0, 1));
            consumer.vertices(new DynamicTestVertex(1, 0, 0, 0, 1, 0, 1));
            consumer.vertices(new DynamicTestVertex(1, 1, 0, 0, 0, 1, 1));
            consumer.vertices(new DynamicTestVertex(0, 1, 0, 1, 1, 0, 1));

            consumer.indices(0, 1, 2);
            consumer.indices(2, 3, 0);

            consumer.draw(ctx);

            //cmd.bindPipeline(COMPUTE);

            //cmd.bindDescriptorSets(COMPUTE);

            //VK14.vkCmdDispatch(cmd.getBuffer(), (int) Math.ceil(ctx.getExtent().width / 16.0), (int) Math.ceil(ctx.getExtent().height / 16.0), 1);

//            cmd.bindPipeline(QUAD);
//
//            consumer.begin();
//            consumer.vertex(new DynamicTestVertex(-1, 1, 0, 1, 1, 1, 1));
//            consumer.vertex(new DynamicTestVertex(1, 1, 0, 1, 1, 1, 1));
//            consumer.vertex(new DynamicTestVertex(-1, -1, 0, 1, 1, 1, 1));
//            consumer.vertex(new DynamicTestVertex(1, -1, 0, 1, 1, 1, 1));
//
//            consumer.index(0, 1, 2, 3);
//
//            cmd.bindDescriptorSets(QUAD);
//            consumer.draw(ctx);
        }

        if (timer.onFrameComplete(AppTimer.DEFAULT_TEST_INTERVAL_BEING_THE_DURATION_OF_9192631770_PERIODS_OF_THE_RADIATION_CORRESPONDING_TO_THE_TRANSITION_BETWEEN_THE_TWO_HYPERFINE_LEVELS_OF_THE_GROUND_STATE_OF_THE_CAESIUM_133_ATOM_EXPRESSED_IN_MILLISECONDS)) {
            System.out.println("FPS: " + timer.fps());
        }
    }

    @Override
    public void onUnload() {
        mesh.free();
        consumer.free();
    }

    @Override
    public void free() {
        tex.free();
    }

    public static class CubeVertexFormat extends Vertex {

        private float x, y, z;
        private float nx, ny, nz;
        private float r, g, b, a;

        public CubeVertexFormat(float x, float y, float z, float nx, float ny, float nz, float r, float g, float b, float a) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.nx = nx;
            this.ny = ny;
            this.nz = nz;
            this.r = r;
            this.g = g;
            this.b = b;
            this.a = a;
        }

        @Override
        public int getByteStride() {
            return 4*10;
        }

        @Override
        public void putSelf(VertexEcoder buf) {
            buf.float3(x, y, z);
            buf.float3(nx, ny, nz);
            buf.float4(r, g, b, a);
        }
    }

}
