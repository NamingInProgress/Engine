package com.vke.api.draw;

import com.vke.core.rendering.draw.DrawContext;

public interface Drawable {

    void draw(DrawContext ctx);
    default void bindIBO(DrawContext ctx) {}
    default void bindVBO(DrawContext ctx) {}

}
