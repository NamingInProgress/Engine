package com.vke.core.rendering.rp;

import com.vke.api.font.FontFace;
import com.vke.core.color.RgbColor;

public final class BasicTextInstance {
    public final int entityId;
    public String text;
    public RgbColor color;
    public int fontScale;
    public float[] mat;
    public FontFace font;
    public int selector;

    public BasicTextInstance(int entityId, String text, RgbColor color, int fontScale, float[] mat, FontFace font, int selector) {
        this.entityId = entityId;
        this.text = text;
        this.color = color;
        this.fontScale = fontScale;
        this.mat = mat;
        this.font = font;
        this.selector = selector;
    }
}
