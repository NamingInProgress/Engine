package com.vke.core.rendering.vulkan;

import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Scissor scissor = (Scissor) o;
        return x == scissor.x && y == scissor.y && w == scissor.w && h == scissor.h;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, w, h);
    }
}
