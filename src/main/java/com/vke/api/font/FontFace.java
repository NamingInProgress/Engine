package com.vke.api.font;

public interface FontFace {

    int FONT_STYLE_REGULAR = 0;
    int FONT_STYLE_ITALIC = 1 << 0;
    int FONT_STYLE_UNDERSCORE = 1 << 1;
    int FONT_STYLE_NEGATIVE = 1 << 2;
    int FONT_STYLE_OUTLINED = 1 << 3;
    int FONT_STYLE_STRIKEOUT = 1 << 4;
    int FONT_STYLE_BOLD = 1 << 5;

    Font get(int flags);

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
