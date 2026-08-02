package com.vke.test.ttf;

import com.vke.api.rendering.abstraction.draw.VertexConsumer;
import com.vke.api.scene.Scene;
import com.vke.core.Context;
import com.vke.core.draw.ShapeRenderer;
import com.vke.core.draw.ShapeRendererVertex;
import com.vke.core.font.ttf.Glyph;
import com.vke.core.font.ttf.GlyphPoint;
import com.vke.core.font.ttf.TTFFile;
import com.vke.core.input.mouse.ScrollDirection;
import com.vke.core.input.service.InputManager;
import com.vke.core.rendering.font.TextRenderer;
import com.vke.core.rendering.graph.GraphContext;
import com.vke.core.services2.Services;
import com.vke.utils.io.Identifier;
import org.joml.Vector2f;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class TTFTestScene extends Scene {

    private ShapeRenderer<ShapeRendererVertex> sr;
    private VertexConsumer<ShapeRendererVertex> vc;
    private TextRenderer tx;
    private TTFFile file;

    private float scale;
    private Glyph bChar;
    private List<Glyph> veerkan22 = new ArrayList<>();

    public TTFTestScene(Identifier name, Context context) {
        super(name, context);
        try (InputStream stream = new FileInputStream("JetBrainsMono-Regular.ttf")) {
            file = new TTFFile(stream);
            scale = 140f / file.head.unitsPerEm;

            for (char c : "B".toCharArray()) {
                int glyphIndex = file.cmap.glyphMap[c];

                veerkan22.add(file.glyf.glyphs[glyphIndex]);
            }

            bChar = veerkan22.getFirst();

            InputManager input = context.service(Services.INPUT_MANAGER);
            var m = input.mouse();
            m.scroll().listen((dx, dy, dir) -> {
                if (dir == ScrollDirection.Down) {

                }
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    long start = System.nanoTime();

    @Override
    public void onPrepareRendering(GraphContext context) {
        tx.getMatrixStack().push();
        tx.getMatrixStack().translate(400, 300, -100);
        //tx.getMatrixStack().rotate((float) Math.toRadians(-45));
        tx.getMatrixStack().rotate(0, (start - System.nanoTime()) / 1_000_000_000f, 0);
        tx.getMatrixStack().scale(scale);

        tx.glyph(bChar);

        tx.getMatrixStack().pop();


        context.put("sr", sr);
        context.put("tx", tx);
    }

    private ShapeRendererVertex v(Vector2f v) {
        return new ShapeRendererVertex(v.x * scale, v.y * scale, 0, 1, 1, 1, 1, 0, 0, sr.currentStackIndex(), null);
    }

    @Override
    public void onLoad() throws Exception {
        this.vc = getRenderer().getVertexConsumerProvider().get(ShapeRendererVertex.TEMPLATE);
        this.sr = new ShapeRenderer<>(getRenderer().getVertexConsumerProvider().get(ShapeRendererVertex.TEMPLATE), ShapeRendererVertex::new);
        this.tx = new TextRenderer(getRenderSystem(), getRenderer().getVertexConsumerProvider());
    }

    @Override
    public void free() {

    }
}
