package com.vke.core.vulkan;

public class Viewport {
    public int x, y, w, h;
    public int minDepth, maxDepth;

    public Viewport() {
        this(0, 0, -1, -1);
    }

    public Viewport(int x, int y, int w, int h) {
        this(x, y, w, h, 0, 1);
    }

    public Viewport(int x, int y, int w, int h, int minDepth, int maxDepth) {
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
        this.minDepth = minDepth;
        this.maxDepth = maxDepth;
    }

    public int width() {
        return w;
    }

    public int height() {
        return h;
    }
}
