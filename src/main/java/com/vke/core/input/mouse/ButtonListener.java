package com.vke.core.input.mouse;

public interface ButtonListener {
    default void onPress(Button button) {}
    default void onRelease(Button button) {}
}
