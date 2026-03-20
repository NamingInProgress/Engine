package com.vke.utils.console;

public class AnsiColors {

    public static final String RESET = "\033[0m";
    public static final String RESET_BOLD = "\033[22m";
    public static final String RESET_ITALIC = "\033[23m";
    public static final String RESET_UNDERLINE = "\033[24m";
    public static final String RESET_BLINKING = "\033[25m";
    public static final String RESET_STRIKETHROUGH = "\033[29m";

    /**  GRAPHICS MODES  **/
    public static final String BOLD = "\033[1m";
    public static final String ITALIC = "\033[3m";
    public static final String UNDERLINE = "\033[4m";
    public static final String BLINKING = "\033[5m";
    public static final String STRIKETHROUGH = "\033[9m";

    /**  BASE FOREGROUND COLORS  **/
    public static final String BLACK = "\033[30m";
    public static final String RED = "\033[31m";
    public static final String GREEN = "\033[32m";
    public static final String YELLOW = "\033[33m";
    public static final String BLUE = "\033[34m";
    public static final String MAGENTA = "\033[35m";
    public static final String CYAN = "\033[36m";
    public static final String WHITE = "\033[37m";
    public static final String GRAY = "\u001B[90m";
    public static final String DEFAULT = "\033[39m";
    public static final String ORANGE = "\033[38;5;208m";
    public static final String GOLD = "\033[38;5;172m";

    /**  BASE BACKGROUND COLORS  **/
    public static final String BACKGROUND_BLACK = "\033[40m";
    public static final String BACKGROUND_RED = "\033[41m";
    public static final String BACKGROUND_GREEN = "\033[42m";
    public static final String BACKGROUND_YELLOW = "\033[43m";
    public static final String BACKGROUND_BLUE = "\033[44m";
    public static final String BACKGROUND_MAGENTA = "\033[45m";
    public static final String BACKGROUND_CYAN = "\033[46m";
    public static final String BACKGROUND_WHITE = "\033[47m";
    public static final String BACKGROUND_DEFAULT = "\033[49m";

    public static String rgb(int r, int g, int b) {
        return "\033[38;2;" + r + ";" + g + ";" + b + "m";
    }

    public static String backgroundRGB(int r, int g, int b) {
        return "\033[48;2;" + r + ";" + g + ";" + b + "m";
    }

}
