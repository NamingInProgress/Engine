package com.vke.api.callbacks.keyboard;

@FunctionalInterface
public interface KeyUpCallback {

    void apply(int keyCode, int scanCode, int mods);

}
