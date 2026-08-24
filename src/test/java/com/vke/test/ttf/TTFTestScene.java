package com.vke.test.ttf;

import com.vke.api.font.FontCursor;
import com.vke.api.game.camera.Camera;
import com.vke.api.game.camera.CameraController;
import com.vke.api.rendering.abstraction.draw.VertexConsumer;
import com.vke.api.scene.Scene;
import com.vke.core.Context;
import com.vke.core.Identifier;
import com.vke.core.draw.ShapeRenderer;
import com.vke.core.draw.ShapeRendererVertex;
import com.vke.core.font.ttf.TTFFont;
import com.vke.core.game.camera.OriginOrthoCamera;
import com.vke.core.game.camera.controllers.CameraController2DMB;
import com.vke.core.rendering.font.TextRenderer;
import com.vke.core.rendering.graph.GraphContext;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class TTFTestScene extends Scene {

    private ShapeRenderer<ShapeRendererVertex> sr;
    private VertexConsumer<ShapeRendererVertex> vc;
    private TextRenderer tx;
    private TTFFont file;

    private FontCursor cursor;

    public TTFTestScene(Identifier name, Context context) {
        super(name, context);
        try (InputStream stream = new FileInputStream("JetBrainsMono-Regular.ttf")) {
            file = new TTFFont(stream);
            this.cursor = new FontCursor(file, 90);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Camera ortho = new OriginOrthoCamera(getRenderSystem());
        CameraController controller = new CameraController2DMB(getRenderSystem());
        ortho.use();
        ortho.setController(controller);
    }

    @Override
    public void onPrepareRendering(GraphContext context) {
        tx.getMatrixStack().push();
        tx.getMatrixStack().translate(0, 300, -100);

        cursor.write("Hello World!");
        tx.accept(cursor, true);

        cursor.setFontSize(120);
        cursor.setY(-100);
        cursor.write("Bigger HW!");
        tx.accept(cursor, true);

        tx.getMatrixStack().pop();

        context.put("sr", sr);
        context.put("tx", tx);
    }

    @Override
    public void onLoad() throws Exception {
        this.vc = getRenderer().getVertexConsumerProvider().get(ShapeRendererVertex.TEMPLATE);
        this.sr = new ShapeRenderer<>(getRenderer().getVertexConsumerProvider().get(ShapeRendererVertex.TEMPLATE), ShapeRendererVertex::new);
        this.tx = new TextRenderer(getRenderSystem(), file, getRenderer().getVertexConsumerProvider());
    }

    @Override
    public void free() {

    }
}
