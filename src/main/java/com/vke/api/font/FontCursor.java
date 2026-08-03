package com.vke.api.font;

import com.vke.core.font.ttf.Glyph;

import java.util.ArrayList;
import java.util.List;

public class FontCursor {

    public final Font font;
    private final float startX, startY, startFontSize;

    private float x, y;
    private float fontSize;

    private int prevGlyphId = -1;
    private final List<GlyphInfo> data = new ArrayList<>();

    private float scale;

    public FontCursor(Font font, float fontSize) {
        this(font, fontSize, 0, 0);
    }

    public FontCursor(Font font, float fontSize, float startX, float startY) {
        this.font = font;
        this.startFontSize = fontSize;
        this.fontSize = fontSize;
        this.startX = startX;
        this.startY = startY;
        this.x = startX;
        this.y = startY;
        this.calculateScale(fontSize);
    }

    public void write(String str) {
        for (int i = 0; i < str.length(); ) {
            int codePoint = str.codePointAt(i);
            Glyph g = font.getGlyph(codePoint);

            glyph(g);

            i += Character.charCount(codePoint);
        }
    }

    public void glyph(Glyph g) {
        float currentScale = getScale();

        if (prevGlyphId != -1) {
            float kernOffset = font.kern(prevGlyphId, g.glyphIndex);
            this.x += kernOffset * currentScale;
        }

        float drawX = this.x + (g.leftSideBearing * currentScale);
        float drawY = this.y;

        data.add(new GlyphInfo(g, fontSize, drawX, drawY));

        this.x += g.advanceWidth * currentScale;

        this.prevGlyphId = g.glyphIndex;
    }

    public void setX(float newX) {
        this.x = newX;
        this.invalidateKern();
    }

    public void setY(float newY) {
        this.y = newY;
        this.invalidateKern();
    }

    public void setFontSize(float newSize) {
        this.fontSize = newSize;
        this.calculateScale(fontSize);
        invalidateKern();
    }

    public float getScale() {
        return scale;
    }

    public List<GlyphInfo> read() {
        return this.data;
    }

    public void clear() {
        data.clear();
        invalidateKern();
    }

    public void reset() {
        clear();
        setX(startX);
        setY(startY);
        setFontSize(startFontSize);
    }

    private void calculateScale(float fontSize) {
        this.scale = fontSize / (float) font.unitsPerEm();
    }

    public void invalidateKern() {
        this.prevGlyphId = -1;
    }

    public record GlyphInfo(Glyph g, float fontSize, float x, float y) {}
}
