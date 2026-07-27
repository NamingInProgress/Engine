package com.vke.test.ttf;

import com.vke.api.rendering.abstraction.draw.VertexConsumer;
import com.vke.api.scene.Scene;
import com.vke.core.Context;
import com.vke.core.draw.ShapeRenderer;
import com.vke.core.draw.ShapeRendererVertex;
import com.vke.core.font.ttf.Glyph;
import com.vke.core.font.ttf.TTFFile;
import com.vke.core.rendering.graph.GraphContext;
import com.vke.utils.io.Identifier;
import org.joml.Vector2f;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class TTFTestScene extends Scene {

    private ShapeRenderer<ShapeRendererVertex> sr;
    private VertexConsumer<ShapeRendererVertex> vc;
    private TTFFile file;

    private float scale;
    private Glyph nullChar;

    public TTFTestScene(Identifier name, Context context) {
        super(name, context);
        try (InputStream stream = new FileInputStream("JetBrainsMono-Bold.ttf")) {
            file = new TTFFile(stream);
            scale = 300f / file.head.unitsPerEm;

            TTFFile.TableInfo glyf = file.tables.get("glyf");

            file.reader.position(glyf.offset);
            nullChar = new Glyph(file.reader);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onPrepareRendering(GraphContext context) {


        sr.getMatrixStack().push();
        sr.getMatrixStack().translate(400, 300, 0);
        sr.color(1, 1, 1, 1);
//        sr.rect((int) xMin, (int) yMin, (int) xMax, (int) yMax);

        int cEndIndex = 0;
        int cStartIndex = 0;
        int cEnd = nullChar.endPointsOfContours[cEndIndex];
        Vector2f[] points = nullChar.points;
        for (int i = 0; i < points.length; i++) {
            Vector2f point = points[i];
            sr.circle((int) (point.x * scale), (int) (point.y * scale), 5, 10);

            if (cEnd == i) {
                vc.begin();
                vc.vertices(v(point), v(points[cStartIndex]));
                vc.indices(0, 1);
                if (cEndIndex + 1 < nullChar.endPointsOfContours.length) {
                    cEnd = nullChar.endPointsOfContours[++cEndIndex];
                }
                cStartIndex = i + 1;
            } else {
                Vector2f nextPoint = points[i + 1];
                vc.begin();
                vc.vertices(v(point), v(nextPoint));
                vc.indices(0, 1);
            }
        }
        sr.getMatrixStack().pop();

        context.put("sr", sr);
        context.put("vc", vc);
    }

    private ShapeRendererVertex v(Vector2f v) {
        return new ShapeRendererVertex(v.x * scale, v.y * scale, 0, 1, 1, 1, 1, 0, 0, 1, null);
    }

    @Override
    public void onLoad() throws Exception {
        this.vc = getRenderer().getVertexConsumerProvider().get(ShapeRendererVertex.TEMPLATE);
        this.sr = new ShapeRenderer<>(getRenderer().getVertexConsumerProvider().get(ShapeRendererVertex.TEMPLATE), ShapeRendererVertex::new);
    }

    @Override
    public void free() {

    }
}
