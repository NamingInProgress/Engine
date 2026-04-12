package com.vke.test.rendering;

import com.vke.api.assets.r.R;
import com.vke.api.draw.IVertexConsumer;
import com.vke.core.mesh.MeshPrefab;
import com.vke.api.draw.Vertex;
import com.vke.api.rendering.abstraction.pipeline.RenderPipeline;
import com.vke.api.rendering.vulkan.buffer.VertexByteSink;
import com.vke.api.rendering.vulkan.pushconstants.PushConstantHandle;
import com.vke.api.scene.Scene;
import com.vke.core.Context;
import com.vke.core.assets.handles.utils.LazyAssetHandle;
import com.vke.core.rendering.draw.DrawContext;
import com.vke.core.services.Services;
import com.vke.core.vulkan.Scissor;
import com.vke.core.vulkan.Viewport;
import com.vke.core.vulkan.VulkanRenderer;
import com.vke.core.vulkan.buffers.premade.mesh.BatchedVertexConsumer;
import com.vke.core.vulkan.buffers.premade.mesh.StaticMeshBuffer;
import com.vke.core.vulkan.command.VulkanCmdBuffers;
import com.vke.core.vulkan.pipeline.VulkanRenderPipeline;
import com.vke.core.window.Window;
import com.vke.utils.io.Identifier;
import org.joml.Matrix4f;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK14;

import java.io.IOException;

public class MainScene extends Scene {

    public MainScene(Identifier name, Context context) {
        super(name, context);
    }

    private VulkanRenderPipeline cubePipeline, dynamicVertsPipeline;
    private LazyAssetHandle<RenderPipeline> CUBE = R.pipelines.get("spinny_cub.pipeline_vt.json");
    private LazyAssetHandle<RenderPipeline> DYNAMIC = R.pipelines.get("dynamic_vertices_test.pipeline.json");

    private PushConstantHandle projMatrixHandle;
    private PushConstantHandle transformMatrixHandle;

    private PushConstantHandle dvProjMatrixHandle;
    private PushConstantHandle dvTransformMatrixHandle;

    private IVertexConsumer<DynamicTestVertex> consumer;

    private StaticMeshBuffer mesh;
    private MeshPrefab prefab;

    @Override
    public void onLoad() {
        cubePipeline = (VulkanRenderPipeline) CUBE.assume(context);
        dynamicVertsPipeline = (VulkanRenderPipeline) DYNAMIC.assume(context);
        
        projMatrixHandle = cubePipeline.resolvePushConstant("world");
        transformMatrixHandle = cubePipeline.resolvePushConstant("translation");

        dvProjMatrixHandle = dynamicVertsPipeline.resolvePushConstant("world");
        dvTransformMatrixHandle = dynamicVertsPipeline.resolvePushConstant("translation");

        consumer = new BatchedVertexConsumer<>(context.getEngine(), context.service(Services.VULKAN_RENDERER), new DynamicTestVertex(0, 0, 0, 0, 0, 0, 0));

        try {
            prefab = R.meshprefabs.get("bear.obj").acquire(context);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        float[] color = {1, 1, 1, 1};

        mesh = StaticMeshBuffer.uploadOnce(context.getEngine(), context.service(Services.VULKAN_RENDERER),
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
    public void drawLoop(DrawContext ctx) {

        try (MemoryStack stack = MemoryStack.stackPush()) {
            VulkanCmdBuffers cmd = (VulkanCmdBuffers) ctx.getCommandBuffer();
            cmd.bindRenderPipeline(CUBE);

            Matrix4f mat = new Matrix4f();
            //mat.setOrtho(0, wp.width(), 0, wp.height(), 0, 1000, true);
            mat.setPerspective((float) Math.toRadians(90), (float) 800 / 600, 0.1f, 1000, true);

            cmd.bindRenderPipeline(CUBE);


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


            cmd.bindRenderPipeline(DYNAMIC);

            dvProjMatrixHandle.write(buf -> buf.putMat4(mat));
            dvTransformMatrixHandle.write(buf -> buf.putMat4(new Matrix4f().translate(-220, -250.0f, -550).scale(scale, scale, scale).rotateY(time * speed)));

            cmd.setPushConstants(DYNAMIC);

            consumer.begin();
//            consumer.vertex(new DynamicTestVertex(0, 0, -550, 1, 0, 0, 1));
//            consumer.vertex(new DynamicTestVertex(200, 0, -550, 0, 1, 0, 1));
//            consumer.vertex(new DynamicTestVertex(200, 200, -550, 0, 0, 1, 1));
//            consumer.vertex(new DynamicTestVertex(0, 200, -500, 1, 1, 0, 1));
//
//            consumer.index(0, 1, 2);
//            consumer.index(2, 3, 0);

            consumer.mesh(prefab.toMesh((prefabVertex) -> new DynamicTestVertex(
                    prefabVertex.position()[0],
                    prefabVertex.position()[1],
                    prefabVertex.position()[2],

                    1,
                    1,
                    1,
                    1
            )));

            consumer.draw(ctx);
            //((VertexConsumer<DynamicTestVertex>) consumer).print();
        }
    }

    @Override
    public void onUnload() {
        mesh.free();
        consumer.free();
    }

    @Override
    public void free() {
    }

    public static class CubeVertexFormat implements Vertex {

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
        public void putSelf(VertexByteSink buf) {
            buf.float3(x, y, z);
            buf.float3(nx, ny, nz);
            buf.float4(r, g, b, a);
        }
    }

    public static class DynamicTestVertex implements Vertex {

        private final float x, y, z;
        private final float r, g, b, a;

        public DynamicTestVertex(float x, float y, float z, float r, float g, float b, float a) {
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
            return 4*7;
        }

        @Override
        public void putSelf(VertexByteSink buf) {
            buf.float3(x, y, z);
            buf.float4(r, g, b, a);
        }
    }

}
