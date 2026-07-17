package com.vke.core.scene.loading;

import com.vke.api.rendering.abstraction.draw.Vertex;
import com.vke.api.rendering.abstraction.draw.VertexConsumer;
import com.vke.api.rendering.abstraction.draw.VertexFactory;
import com.vke.api.rendering.abstraction.renderer.RenderSystem;
import com.vke.api.rendering.abstraction.renderer.commands.CommandBuffer;
import com.vke.api.rendering.abstraction.renderer.data.Texture;
import com.vke.api.rendering.abstraction.renderer.data.VertexEncoder;
import com.vke.api.rendering.abstraction.renderer.enums.LoadOp;
import com.vke.api.rendering.abstraction.renderer.enums.StoreOp;
import com.vke.core.rendering.graph.GraphContext;
import com.vke.core.rendering.graph.RenderGraph;
import com.vke.api.rendering.abstraction.rendergraph.RenderPass;
import com.vke.core.rendering.graph.RenderPassInstance;
import com.vke.core.rendering.pipeline.RenderPipelines;

import java.util.List;

public class RectLoadingSceneRenderPass extends RenderPass {
    //@DataBinding("progress")
    private float progress;

    private VertexConsumer<V> vc;

    public RectLoadingSceneRenderPass(RenderSystem renderSystem, RenderPassInstance instance) {
        super(renderSystem, instance);
    }

    @Override
    public void onLoad() {
        this.vc = renderSystem.renderer().getVertexConsumerProvider().get(V.TEMPLATE);
    }

    @Override
    public void execute(CommandBuffer cmd, GraphContext context) {
        Texture color = instance.getOutputTexture("output");
        cmd.beginRendering(new CommandBuffer.RenderingInfo(List.of(
                new CommandBuffer.AttachmentInfo(color, LoadOp.CLEAR, StoreOp.STORE, new float[]{ 0.2f, 0.3f, 0.3f, 1.0f })
        ), null));

        //float right = -1.0f + 2.0f * state.get();
        float right = 0f;

        RenderPipelines.LOAD.use();
        vc.beginFrame();

        vc.vertices(new V(-1, -1, 1, 0, 0, 1));
        vc.vertices(new V(right, -1, 1, 0, 0, 1));
        vc.vertices(new V(right,  1, 1, 0, 0, 1));
        vc.vertices(new V(-1,  1, 1, 0, 0, 1));

        vc.indices(0, 1, 2, 2, 0, 3);
        vc.draw();

        cmd.endRendering();
    }

    static class V implements Vertex {
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
        public void putSelf(VertexEncoder buf) {
            buf.float2(x, y);
            buf.float4(r, g, b, a);
        }
    }

}
