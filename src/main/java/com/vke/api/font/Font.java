package com.vke.api.font;

import com.vke.core.font.ttf.Glyph;

import java.util.List;

public interface Font {
    Glyph getGlyph(int codePoint);
    List<Glyph> getStringGlyphs(String s);
    int unitsPerEm();
    Metadata meta();

    /**
     * @param beforeIndex - The index of the glyph directly before the current glyph.
     * @param currentIndex - The index of the glyph currently adding
     * @return - The kerning offset between them. If a font does not support kerning this may be 0.
     */
    float kern(int beforeIndex, int currentIndex);

    record Metadata(int numGlyphs, int unitsPerEm, SpacingInfo spacingInfo, CaretInfo caretInfo) {}

    record SpacingInfo(short ascent, short descent, short lineGap, int unscaledLineHeight) {}
    record CaretInfo(short caretRise, short caretRun, short caretOffset) {}
}
