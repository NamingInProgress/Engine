package com.vke.core.input.mouse;

public enum Button {
    Left,
    Right,
    Middle,
    B4,
    B5,
    B6,
    B7,
    B8;

    public static Button fromGlfw(int code) {
        return Button.values()[code];
    }

    public int toGlfw() {
        return ordinal();
    }
}
