package com.vke.api.rendering.abstraction.draw;

import com.vke.api.rendering.abstraction.renderer.data.Texture;

public class TextureRegion implements QuadTexture {

    private final Texture parent;
    private final float[] uv;

    public TextureRegion(Texture parent, float u, float v, float texWidth, float texHeight) {
        this.parent = parent;
        this.uv = new float[]{ u, v, texWidth, texHeight };
    }

    public static TextureRegion cropPixels(Texture parent, int x, int y, int w, int h) {
        float scalex = 1f / (float) parent.width();
        float scaley = 1f / (float) parent.height();
        float fx = x;
        float fy = y;
        float fw = w;
        float fh = h;
        return new TextureRegion(parent, fx * scalex, fy * scaley, fw * scalex, fh * scaley);
    }

    @Override
    public float[] uvFor() {
        return uv;
    }

    @Override
    public Texture texture() {
        return parent;
    }
}
