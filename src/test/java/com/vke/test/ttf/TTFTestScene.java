package com.vke.test.ttf;

import com.vke.api.assets.r.R;
import com.vke.api.font.FontFace;
import com.vke.api.rendering.abstraction.draw.VertexConsumer;
import com.vke.api.scene.Scene;
import com.vke.core.Context;
import com.vke.core.Identifier;
import com.vke.core.assets.handles.LazyAssetHandle;
import com.vke.core.color.RgbColor;
import com.vke.core.draw.ShapeRenderer;
import com.vke.core.draw.ShapeRendererVertex;
import com.vke.impl.gameobject.controller.FreecamController;
import com.vke.core.game.scene.service.HierarchyManager;
import com.vke.core.rendering.font.TextRenderer;
import com.vke.core.rendering.graph2.GraphContext;
import com.vke.core.services2.Services;
import com.vke.impl.gameobject.CameraGameObject;

public class TTFTestScene extends Scene {

    private ShapeRenderer<ShapeRendererVertex> sr;
    private VertexConsumer<ShapeRendererVertex> vc;
    private TextRenderer tx;

    private CameraGameObject cam;

    private HierarchyManager hierarchyManager;

    private LazyAssetHandle<FontFace> jetbrains_mono = R.fonts.get("fonts/JetBrainsMono");

    public TTFTestScene(Identifier name, Context context) {
        super(name, context);

        hierarchyManager = context.service(Services.HIERARCHY);
    }

    @Override
    public void onPrepareRendering(GraphContext context) {
        hierarchyManager.updateTransforms();

        tx.getMatrixStack().push();
        tx.getMatrixStack().translate(0, 0, 0);
//        tx.getMatrixStack().rotate(0, System.nanoTime() / 1_000_000_000f, 0);

        tx.color(RgbColor.CYAN);
        tx.drawText(jetbrains_mono, 90, "R", "Hello World!");

        tx.getMatrixStack().pop();

        context.put("sr", sr);
        context.put("tx", tx);
    }

    @Override
    public void onLoad() throws Exception {
        this.vc = getRenderSystem().vcp().get(ShapeRendererVertex.TEMPLATE);
        this.sr = new ShapeRenderer<>(getRenderSystem().vcp().get(ShapeRendererVertex.TEMPLATE), ShapeRendererVertex::new);
        this.tx = new TextRenderer(getRenderSystem());

        cam = new CameraGameObject(context);
        cam.spawn();
        cam.setIsOrtho(false);
        cam.control(new FreecamController(context, 2, 0.0025f));

        getRenderSystem().frameDataManager().setCamera(cam);
    }

    @Override
    public void free() {

    }
}
