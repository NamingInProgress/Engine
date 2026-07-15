package com.vke.test.rendering;

import com.vke.api.assets.r.R;
import com.vke.api.rendering.abstraction.draw.Vertex;
import com.vke.api.rendering.abstraction.draw.VertexConsumer;
import com.vke.api.rendering.abstraction.renderer.RenderResourceManager;
import com.vke.api.rendering.abstraction.renderer.data.StaticMesh;
import com.vke.api.rendering.abstraction.renderer.data.VertexEncoder;
import com.vke.api.rendering.vulkan.descriptors2.handles.buf.BufferHandle;
import com.vke.api.rendering.vulkan.descriptors2.handles.other.CISHandle;
import com.vke.core.draw.ShapeRenderer;
import com.vke.core.draw.ShapeRendererVertex;
import com.vke.api.rendering.abstraction.renderer.data.Texture;
import com.vke.api.rendering.abstraction.renderer.pipeline.RenderPipeline;
import com.vke.api.rendering.vulkan.pushconstants.PushConstantHandle;
import com.vke.core.Context;
import com.vke.core.assets.handles.LazyAssetHandle;
import com.vke.core.mesh.MeshPrefab;
import com.vke.core.rendering.vulkan.pipeline.VulkanRenderPipeline;
import com.vke.core.rendering.Samplers;
import com.vke.demo.DemoScene;
import com.vke.utils.io.Identifier;
import org.joml.Matrix4f;

import java.io.IOException;

public class BatchedVertexConsumerTest extends RenderingScene {

    public BatchedVertexConsumerTest(Identifier name, Context context) {
        super(name, context);
    }

    private LazyAssetHandle<RenderPipeline> PL = R.pipelines.get("batched_consumer_test.pipeline.json");
    private LazyAssetHandle<RenderPipeline> CUBE = R.pipelines.get("spinny_cub.pipeline_vt.json");
    private LazyAssetHandle<RenderPipeline> FSQ = R.pipelines.get("fullscreen_quad.pipeline.json");
    private VulkanRenderPipeline pipeline, cubePipeline, fullscreenQuadPipeline;
    private Texture scaryVK;
    private Texture missing;
    private Texture bear_performance;

    private PushConstantHandle projMatrixHandle, transformMatrixHandle;
    private PushConstantHandle proj, transform;
    private BufferHandle matricesBuf;

    private CISHandle depthTexFSQ;

    private StaticMesh mesh;
    private MeshPrefab prefab;

    private VertexConsumer<ShapeRendererVertex> consumer;
    private ShapeRenderer<ShapeRendererVertex> shapeRenderer;
    private VertexConsumer<FullscreenQuadVertex> fsqc;

    @Override
    public void onLoad() {
        pipeline = (VulkanRenderPipeline) PL.assume(context);
        cubePipeline = (VulkanRenderPipeline) CUBE.assume(context);
        fullscreenQuadPipeline = (VulkanRenderPipeline) FSQ.assume(context);
        fsqc = getRenderer().getVertexConsumerProvider().get(FullscreenQuadVertex.TEMPLATE);

        projMatrixHandle = cubePipeline.resolvePushConstant("world");
        transformMatrixHandle = cubePipeline.resolvePushConstant("translation");

        this.depthTexFSQ = fullscreenQuadPipeline.uniform("tex");

//        proj = pipeline.uniform("stuff.world");
//        transform = pipeline.uniform("stuff.translation");
        proj = pipeline.resolvePushConstant("world");
        transform = pipeline.resolvePushConstant("translation");
        matricesBuf = pipeline.uniform("matrixStack");

        this.consumer = getRenderer().getVertexConsumerProvider().get(ShapeRendererVertex.TEMPLATE);
        this.shapeRenderer = new ShapeRenderer<>(consumer, ShapeRendererVertex.FACTORY);

        this.scaryVK = R.textures.get("scaryvulkan.png").assume(context);
        this.missing = R.textures.get("missing.png").assume(context);
        this.bear_performance = R.textures.get("bear_performance.png").assume(context);

        try {
            prefab = R.meshprefabs.get("bear.obj").acquire(context);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        float[] color = {1, 1, 1, 1};

        RenderResourceManager resManager = getRenderer().resourceManager();

        mesh = resManager.uploadStaticMesh(
                prefab.toMesh((prefabVertex -> new DemoScene.CubeVertexFormat(
                        prefabVertex.position()[0],
                        prefabVertex.position()[1],
                        prefabVertex.position()[2],

                        prefabVertex.normal()[0],
                        prefabVertex.normal()[1],
                        prefabVertex.normal()[2],

                        color[0],
                        color[1],
                        color[2],
                        color[3])))
        );
    }

    @Override
    public void onDraw() {
        // Draw
        Matrix4f mat = new Matrix4f();
        mat.setOrtho(0, context.getEngine().getWindow().getSize().width(), 0, context.getEngine().getWindow().getSize().height(), 0, 1000, true);
        proj.write(slice -> slice.putMat4(mat));
        transform.write(slice -> slice.putMat4(new Matrix4f().translate(0, 0, 0)));
        getRenderSystem().getCurrentCommandBuffer().bindPipeline(PL);

        getRenderSystem().getCurrentCommandBuffer().setPushConstants(PL);

        var ms = shapeRenderer.getMatrixStack();

        ms.push();
        ms.translate(400, 300, 0);
        ms.rotate(System.nanoTime() / 1_000_000_000f);
        //ms.translate(-400, -300, 0);
        shapeRenderer.texture(scaryVK);
        shapeRenderer.color(0, 0, 0, 1);
        shapeRenderer.circle(0, 0, 100, 50);
        ms.pop();

        ms.push();
        ms.translate(100, 100, 0);
        ms.rotate((float) Math.toRadians(45));
        shapeRenderer.color(1, 0, 0, 1);
        shapeRenderer.rect(0, 0, 100, 100);
        ms.pop();

//        shapeRenderer.texture(bear_performance);
//        shapeRenderer.color(0, 0, 0, 1);
//        shapeRenderer.rect(0, 0, 800, 600);

        shapeRenderer.texture(missing);
        shapeRenderer.color(0, 0, 0, 1);
        shapeRenderer.ovalArc(200, 200, 100, 50, 0, 90, 32);

        matricesBuf.write((slice) -> {
            shapeRenderer.getMatrixStack().upload(slice);
        });
        getRenderSystem().getCurrentCommandBuffer().bindDescriptorSets(PL);
        shapeRenderer.draw();

        getRenderSystem().getCurrentCommandBuffer().bindPipeline(CUBE);
        getRenderSystem().getCurrentCommandBuffer().bindDescriptorSets(CUBE);
        Matrix4f model = new Matrix4f();

        float time = (System.nanoTime() / 1_000_000_000.0f);

        float speed = 1.0f;

        float scale = 10;
        model.identity()
                .translate(200.0f, -250.0f, -550)
                .scale(scale, scale, scale)
                .rotateY(time * speed);

        projMatrixHandle.write(buf -> buf.putMat4(new Matrix4f().setPerspective((float) Math.toRadians(90), (float) 800 / 600, 0.1f, 1000, true)));
        transformMatrixHandle.write(buf -> buf.putMat4(model));

        getRenderSystem().getCurrentCommandBuffer().setPushConstants(CUBE);

        mesh.draw();

        fsqc.vertices(new FullscreenQuadVertex(-1.0f, -1.0f, 0.0f, 0.0f)); // 0
        fsqc.vertices(new FullscreenQuadVertex( 1.0f, -1.0f, 1.0f, 0.0f)); // 1
        fsqc.vertices(new FullscreenQuadVertex( 1.0f,  1.0f, 1.0f, 1.0f)); // 2
        fsqc.vertices(new FullscreenQuadVertex(-1.0f,  1.0f, 0.0f, 1.0f)); // 3
        fsqc.indices(0, 1, 2, 0, 2, 3);

        var tex = getRenderSystem().swapchain().depthTarget();
        tex.useInShader();

        depthTexFSQ.set(tex, Samplers.LINEAR);
        getRenderSystem().getCurrentCommandBuffer().bindPipeline(FSQ);
        getRenderSystem().getCurrentCommandBuffer().bindDescriptorSets(FSQ);
        fsqc.draw();
    }

    @Override
    public void onUnload() {
    }

    @Override
    public void free() {}

    public static class FullscreenQuadVertex implements Vertex {

        public static final FullscreenQuadVertex TEMPLATE = new FullscreenQuadVertex(0, 0, 0, 0);

        private float x, y, u, v;

        public FullscreenQuadVertex(float x, float y, float u, float v) {
            this.x = x;
            this.y = y;
            this.u = u;
            this.v = v;
        }

        @Override
        public int getByteStride() {
            return 4 * Float.BYTES;
        }

        @Override
        public void putSelf(VertexEncoder buf) {
            buf.float2(x, y);
            buf.float2(u, v);
        }
    }

}
