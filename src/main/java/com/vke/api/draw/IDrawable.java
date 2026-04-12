package com.vke.api.draw;

import com.vke.core.rendering.draw.DrawContext;

public interface IDrawable {

    void draw(DrawContext ctx);
    void bindIBO(DrawContext ctx);
    void bindVBO(DrawContext ctx);

}
