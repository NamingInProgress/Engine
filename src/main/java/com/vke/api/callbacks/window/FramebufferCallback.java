package com.vke.api.callbacks.window;

@FunctionalInterface
public interface FramebufferCallback {

    void apply(int width, int height);

}
