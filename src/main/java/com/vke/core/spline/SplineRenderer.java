package com.vke.core.spline;

import com.vke.api.draw.Drawable;
import com.vke.api.draw.Vertex;
import com.vke.api.draw.VertexConsumer;
import com.vke.api.draw.VertexFactory;
import com.vke.core.geom.Rect;
import com.vke.core.rendering.draw.DrawContext;

public class SplineRenderer<V extends Vertex> implements Drawable {
    private final VertexConsumer<V> consumer;
    private final VertexFactory<V> factory;

    public SplineRenderer(VertexConsumer<V> consumer, VertexFactory<V> factory) {
        this.consumer = consumer;
        this.factory = factory;
    }

    public void drawSpline(Rect area, Spline spline, SplineStyle style, float tolerance) {
        FlattenedCurve flattenedCurve = spline.flatten(tolerance);

    }

    @Override
    public void draw(DrawContext ctx) {
        consumer.draw(ctx);
    }
}
