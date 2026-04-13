package com.vke.api.app;

import com.vke.core.rendering.draw.DrawContext;

public interface Framable {

    default void preFrame() {}
    default void preRendering(DrawContext ctx) {}
    default void onDraw(DrawContext ctx) {}
    default void postRendering(DrawContext ctx) {}
    default void postFrame() {}

}
