package com.vke.api.font;

import com.carrotsearch.hppc.IntObjectHashMap;

public class FontFaceImpl implements FontFace {

    private final Font[] fonts;
    private final IntObjectHashMap<Font> styles = new IntObjectHashMap<>();

    public FontFaceImpl(Font[] fonts) {
        this.fonts = fonts;

        for (Font font : fonts) {
            styles.put(font.meta().style(), font);
        }
    }

    @Override
    public Font get(int flags) {
        return styles.get(flags);
    }

    @Override
    public Font[] availableFonts() {
        return fonts;
    }
}
