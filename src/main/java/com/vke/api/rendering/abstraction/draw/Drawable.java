package com.vke.api.rendering.abstraction.draw;

public interface Drawable {

    void draw();
    void drawInstanced(int instanceCount);
    void drawInstanced(int instanceCount, int firstInstance);

}
