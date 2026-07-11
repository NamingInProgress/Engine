package com.vke.api.draw;

import com.vke.core.rendering.draw.FrameContext;

public interface Drawable {

    void draw(FrameContext ctx);
    void drawInstanced(FrameContext ctx, int instanceCount);
    default void bindIBO(FrameContext ctx) {}
    default void bindVBO(FrameContext ctx) {}

}
