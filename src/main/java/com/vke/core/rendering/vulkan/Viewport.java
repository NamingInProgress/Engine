package com.vke.core.rendering.vulkan;

import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Viewport viewport = (Viewport) o;
        return x == viewport.x && y == viewport.y && w == viewport.w && h == viewport.h && minDepth == viewport.minDepth && maxDepth == viewport.maxDepth;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, w, h, minDepth, maxDepth);
    }
}
