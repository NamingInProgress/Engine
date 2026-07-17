package com.vke.core.rendering.vulkan;

public class Scissor {
    public int x, y, w, h;

    public Scissor() {
        this.w = -1;
        this.h = -1;
    }

    public Scissor(int x, int y, int w, int h) {
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
    }

}
