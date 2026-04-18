package com.vke.core.input.keyboard;

public interface KeyListener {
    default void onType(int utf32Codepoint) {}
    default void onPress(Key key) { }
    default void onRelease(Key key) {}
}
