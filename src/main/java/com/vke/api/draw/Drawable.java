package com.vke.api.draw;

import com.vke.core.rendering.draw.FrameContext;

public interface Drawable {

    void draw(FrameContext ctx);
    default void bindIBO(FrameContext ctx) {}
    default void bindVBO(FrameContext ctx) {}

}
