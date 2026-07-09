package com.vke.core.scene.loading;

import com.vke.api.draw.Vertex;
import com.vke.api.draw.VertexConsumer;
import com.vke.api.draw.VertexFactory;
import com.vke.api.rendering.abstraction.Renderer;
import com.vke.api.rendering.vulkan.buffer.VertexByteSink;
import com.vke.api.scene.LoadingScene;
import com.vke.core.Context;
import com.vke.core.rendering.draw.FrameContext;
import com.vke.core.rendering.pipeline.RenderPipelines;
import com.vke.utils.io.Identifier;

public class RectLoadingScene extends LoadingScene {
    private VertexConsumer<V> vc;
    private float p;

    public RectLoadingScene(Identifier name, Context context) {
        super(name, context);
        this.p = 0.0f;
    }

    @Override
    public void onLoad() throws Exception {
        Renderer renderer = context.service(context.getEngine().rendererType().serviceName);
        this.vc = renderer.getVertexConsumerProvider().get(V.TEMPLATE);
    }

    @Override
    public void onUnload() throws Exception {
        free();
    }

    @Override
    public void onAssetStartLoad(AssetDesc desc) {

    }

    @Override
    public void onAssetEndLoad(AssetDesc desc) {
        p = ((float) desc.position()) / ((float) desc.totalAmount());
    }

    @Override
    public void onAssetException(AssetDesc desc, Throwable exception) {

    }

    @Override
    public void onLoadingComplete() {
        try {
            completeLoading();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onDraw(FrameContext ctx) {
        float right = -1.0f + 2.0f * p;

        RenderPipelines.LOAD.use(ctx);
        vc.beginFrame();

        vc.vertices(new V(-1, -1, 1, 0, 0, 1));
        vc.vertices(new V(right, -1, 1, 0, 0, 1));
        vc.vertices(new V(right,  1, 1, 0, 0, 1));
        vc.vertices(new V(-1,  1, 1, 0, 0, 1));

        vc.indices(0, 1, 2, 2, 0, 3);
        vc.draw(ctx);
    }

    @Override
    public void free() {
        vc.free();
    }

    static class V extends Vertex {
        public static final V TEMPLATE = new V(0, 0, 0, 0, 0, 0);
        public static final VertexFactory<V> FACTORY = (x, y, z, r, g, b, a, u, v, matId, texture) -> new V(x, y, r, g, b, a);

        private final float x, y;
        private final float r, g, b, a;

        V(float x, float y, float r, float g, float b, float a) {
            this.x = x;
            this.y = y;
            this.r = r;
            this.g = g;
            this.b = b;
            this.a = a;
        }

        @Override
        public int getByteStride() {
            return 4 * 6;
        }

        @Override
        public void putSelf(VertexByteSink buf) {
            buf.float2(x, y);
            buf.float4(r, g, b, a);
        }
    }
}
