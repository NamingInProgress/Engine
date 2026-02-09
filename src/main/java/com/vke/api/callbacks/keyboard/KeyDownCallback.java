package com.vke.api.callbacks.keyboard;

@FunctionalInterface
public interface KeyDownCallback {

    void apply(int keyCode, int scanCode, int mods);

}
