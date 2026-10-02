package com.vke.test.ttf;

import com.vke.api.assets.r.R;
import com.vke.api.font.FontCursor;
import com.vke.api.font.FontFace;
import com.vke.api.game.camera.Camera;
import com.vke.api.game.camera.CameraController;
import com.vke.api.rendering.abstraction.draw.VertexConsumer;
import com.vke.api.scene.Scene;
import com.vke.core.Context;
import com.vke.core.Identifier;
import com.vke.core.assets.handles.LazyAssetHandle;
import com.vke.core.draw.ShapeRenderer;
import com.vke.core.draw.ShapeRendererVertex;
import com.vke.core.font.ttf.TTFFont;
import com.vke.core.game.camera.OriginOrthoCamera;
import com.vke.core.game.camera.controllers.CameraController2DMB;
import com.vke.core.game.camera.controllers.FreecamController;
import com.vke.core.game.scene.service.HierarchyManager;
import com.vke.core.rendering.font.TextRenderer;
import com.vke.core.rendering.graph2.GraphContext;
import com.vke.core.services2.Services;
import com.vke.impl.gameobject.CameraGameObject;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class TTFTestScene extends Scene {

    private ShapeRenderer<ShapeRendererVertex> sr;
    private VertexConsumer<ShapeRendererVertex> vc;
    private TextRenderer tx;
    private TTFFont file;

    private FontCursor cursor;

    private CameraGameObject cam;

    private HierarchyManager hierarchyManager;

    private LazyAssetHandle<FontFace> jetbrains_mono = R.fonts.get("fonts/JetBrainsMono");

    public TTFTestScene(Identifier name, Context context) {
        super(name, context);
//        try (InputStream stream = new FileInputStream("JetBrainsMono-Bold.ttf")) {
//            file = new TTFFont(stream);
//            this.cursor = new FontCursor(file, 90);
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }

        hierarchyManager = context.service(Services.HIERARCHY);
    }

    @Override
    public void onPrepareRendering(GraphContext context) {
        hierarchyManager.updateTransforms();

        tx.getMatrixStack().push();
        tx.getMatrixStack().translate(0, 300, -100);

        cursor.write("Hello World!");
        tx.accept(cursor, true);

        cursor.setFontSize(12);
        cursor.setY(-100);
        cursor.write("Bigger HW!");
        tx.accept(cursor, true);

        tx.getMatrixStack().pop();

        context.put("sr", sr);
        context.put("tx", tx);
    }

    @Override
    public void onLoad() throws Exception {
        cursor = new FontCursor(jetbrains_mono.assume(context).get(FontFace.FONT_STYLE_REGULAR), 90);

        this.vc = getRenderSystem().vcp().get(ShapeRendererVertex.TEMPLATE);
        this.sr = new ShapeRenderer<>(getRenderSystem().vcp().get(ShapeRendererVertex.TEMPLATE), ShapeRendererVertex::new);
        this.tx = new TextRenderer(getRenderSystem(), jetbrains_mono.assume(context).get(FontFace.FONT_STYLE_REGULAR));

        cam = new CameraGameObject(context);
        cam.spawn();
        cam.setIsOrtho(false);
        cam.control(new FreecamController(context));

        getRenderSystem().frameDataManager().setCamera(cam);
    }

    @Override
    public void free() {

    }
}
