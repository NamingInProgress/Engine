package com.vke.core.rendering.vulkan.extent;

public class Extent3D extends Extent2D {
    public final int depth;

    public Extent3D(int width, int height, int depth) {
        super(width, height);
        this.depth = depth;
    }

    public Extent3D(Extent2D ext, int depth) {
        super(ext.width, ext.height);
        this.depth = depth;
    }

}
