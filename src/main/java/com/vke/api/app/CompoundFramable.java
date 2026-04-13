package com.vke.api.app;

import com.vke.core.rendering.draw.DrawContext;
import com.vke.utils.iter.Iter;

public interface CompoundFramable extends Framable {

    Iter<Framable> children();

    @Override
    default void preFrame() { children().forEach(Framable::preFrame); }

    @Override
    default void preRendering(DrawContext ctx) { children().forEach(f -> f.preRendering(ctx)); }

    @Override
    default void onDraw(DrawContext ctx) { children().forEach((f) -> f.onDraw(ctx)); }

    @Override
    default void postRendering(DrawContext ctx) { children().forEach(f -> f.postRendering(ctx)); }

    @Override
    default void postFrame() { children().forEach(Framable::postFrame); }

}
