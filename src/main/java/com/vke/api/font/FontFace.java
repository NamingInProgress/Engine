package com.vke.api.font;

import com.carrotsearch.hppc.CharIntHashMap;

public interface FontFace {

    CharIntHashMap selectorToValue = new CharIntHashMap();

    int FONT_STYLE_REGULAR = 0;
    int FONT_STYLE_ITALIC = 1 << 0;
    int FONT_STYLE_UNDERSCORE = 1 << 1;
    int FONT_STYLE_NEGATIVE = 1 << 2;
    int FONT_STYLE_OUTLINED = 1 << 3;
    int FONT_STYLE_STRIKEOUT = 1 << 4;
    int FONT_STYLE_BOLD = 1 << 5;

    Font get(int flags);

    default Font get(String selector) {
        return get(toFlags(selector));
    }

    default int toFlags(String selector) {
        if (selectorToValue.isEmpty()) fillSelectorToValue();

        int combination = 0;
        for (char c : selector.toCharArray()) {
            int v = selectorToValue.getOrDefault(c, -1);
            if (v == -1) throw new IllegalStateException("Found invalid selector `" + c + "` for font face!");
            combination |= v;
        }

        return combination;
    }

    private void fillSelectorToValue() {
        selectorToValue.put('R', FONT_STYLE_REGULAR);
        selectorToValue.put('I', FONT_STYLE_ITALIC);
        selectorToValue.put('U', FONT_STYLE_UNDERSCORE);
        selectorToValue.put('N', FONT_STYLE_NEGATIVE);
        selectorToValue.put('O', FONT_STYLE_OUTLINED);
        selectorToValue.put('S', FONT_STYLE_STRIKEOUT);
        selectorToValue.put('B', FONT_STYLE_BOLD);
    }

    Font[] availableFonts();

    static int[] orderedStyles() {
        return new int[]{
                FONT_STYLE_ITALIC,
                FONT_STYLE_UNDERSCORE,
                FONT_STYLE_NEGATIVE,
                FONT_STYLE_OUTLINED,
                FONT_STYLE_STRIKEOUT,
                FONT_STYLE_BOLD
        };
    }

}
