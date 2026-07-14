package com.vke.api.window.callbacks.window;

@FunctionalInterface
public interface FramebufferCallback {

    void apply(int width, int height);

}
