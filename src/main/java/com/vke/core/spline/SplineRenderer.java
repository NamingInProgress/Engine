package com.vke.core.spline;

import com.vke.api.draw.Drawable;
import com.vke.api.draw.Vertex;
import com.vke.api.draw.VertexConsumer;
import com.vke.api.draw.VertexFactory;
import com.vke.core.geom.Rect;
import com.vke.core.rendering.draw.DrawContext;
import com.vke.core.spline.poly.PolyLine;
import com.vke.core.spline.triangulate.TriangulatedPolyLine;

public class SplineRenderer<V extends Vertex> implements Drawable {
    private final VertexConsumer<V> consumer;
    private final VertexFactory<V> factory;

    public SplineRenderer(VertexConsumer<V> consumer, VertexFactory<V> factory) {
        this.consumer = consumer;
        this.factory = factory;
    }

    private V v(float x, float y) {
        return factory.apply(x, y, 0, 1, 0, 0, 1, 0, 0, null);
    }

    public void drawSpline(Rect area, Spline spline, SplineStyle style, float tolerance) {
        PolyLine polyLine = spline.flatten(tolerance);
        TriangulatedPolyLine tpl;
        if (style.filled) {
            tpl = polyLine.triangulateFilled(style);
            if (tpl == null) {
                return;
            }
        } else {
            tpl = polyLine.triangulateContour(style.joinStyle, style.capStyle, style.strokeWidth);
        }
        consumer.begin();
        tpl.forEachVertex((x, y) -> {
            consumer.vertices(v(x, y));
        });
        consumer.indices(tpl.getIndices());
    }

    @Override
    public void draw(DrawContext ctx) {
        consumer.draw(ctx);
    }
}
