package com.vke.core.callbacks;

import com.vke.api.callbacks.keyboard.KeyDownCallback;
import com.vke.api.callbacks.keyboard.KeyUpCallback;
import org.lwjgl.glfw.GLFW;

import java.util.HashSet;
import java.util.Set;

public class KeyboardCallbacks {

    private static final Set<KeyDownCallback> keyDown = new HashSet<>();
    private static final Set<KeyUpCallback> keyUp = new HashSet<>();

    public static void keyDown(KeyDownCallback callback) {
        keyDown.add(callback);
    }

    public static void keyUp(KeyUpCallback callback) {
        keyUp.add(callback);
    }

    public static boolean isKeyPressed(long window, int keyCode) {
        return GLFW.glfwGetKey(window, keyCode) == GLFW.GLFW_PRESS;
    }

    public static void onKey(long window, int key, int scanCode, int action, int mods) {
        if (action == GLFW.GLFW_PRESS) {
            keyDown.forEach(c -> c.apply(key, scanCode, mods));
        } else {
            keyUp.forEach(c -> c.apply(key, scanCode, mods));
        }
    }

}
