package com.vke.demo.test2d;

import com.vke.api.game.camera.CameraController;
import com.vke.api.rendering.abstraction.draw.VertexConsumer;
import com.vke.api.rendering.abstraction.renderer.RenderSystem;
import com.vke.api.rendering.abstraction.renderer.commands.CommandBuffer;
import com.vke.api.rendering.abstraction.renderer.data.Texture;
import com.vke.api.rendering.abstraction.renderer.enums.LoadOp;
import com.vke.api.rendering.abstraction.renderer.enums.StoreOp;
import com.vke.core.draw.ShapeRenderer;
import com.vke.core.draw.ShapeRendererVertex;
import com.vke.core.game.camera.controllers.FreecamController;
import com.vke.core.game.object.controller.GameObjectController;
import com.vke.core.rendering.graph2.GraphContext;
import com.vke.core.rendering.graph2.GraphTexture;
import com.vke.core.rendering.graph2.RenderGraph;
import com.vke.core.rendering.graph2.renderpass.RenderPass;
import com.vke.core.rendering.pipeline.RenderPipelines;
import com.vke.impl.gameobject.CameraGameObject;

import java.util.List;

public class Test2DRenderPass extends RenderPass {
    private ShapeRenderer<ShapeRendererVertex> sr;
    private VertexConsumer<ShapeRendererVertex> vc;
    private GraphTexture output;

    public Test2DRenderPass(RenderSystem sys, RenderGraph graph, Def def) {
        super(sys, graph, def);
    }

    @Override
    public void onLoad() {
        vc = sys.vcp().get(ShapeRendererVertex.TEMPLATE);
        sr = new ShapeRenderer<>(vc, ShapeRendererVertex.FACTORY);

        output = searchOutputTexture("output");

        CameraGameObject camera = new CameraGameObject(sys);
        camera.spawn();
        camera.setIsOrtho(true);

        sys.frameDataManager().setCamera(camera);
    }

    @Override
    public void execute(CommandBuffer cmd, GraphContext context) {
        cmd.beginRendering(new CommandBuffer.RenderingInfo(List.of(
                new CommandBuffer.AttachmentInfo(output.extract(), LoadOp.CLEAR, StoreOp.STORE, new float[]{ 0.2f, 0.3f, 0.3f, 1.0f })
        ), null));

        RenderPipelines.SHAPE.use();

        Texture tex = context.get("texture");
        sr.texture(tex);
        sr.z(-1);
        int size = 500;
        sr.rect(-size / 2, -size / 2, size, size);

        RenderPipelines.SHAPE.setMatrices(sr.getMatrixStack());
        sr.draw();

        cmd.endRendering();
    }
}
