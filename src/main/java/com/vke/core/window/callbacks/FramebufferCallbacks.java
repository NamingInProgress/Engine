package com.vke.core.window.callbacks;

import com.vke.api.window.callbacks.window.FramebufferCallback;
import com.vke.api.window.callbacks.window.MinimizeCallback;

import java.util.HashSet;
import java.util.Set;

public class FramebufferCallbacks {

    private static final Set<FramebufferCallback> resize = new HashSet<>();

    public static void resize(FramebufferCallback callback) {
        resize.add(callback);
    }

    public static void onResize(long window, int width, int height) {
        resize.forEach(c -> c.apply(width, height));
    }

}
