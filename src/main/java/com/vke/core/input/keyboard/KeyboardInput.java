package com.vke.core.input.keyboard;

import com.vke.api.app.Framable;
import com.vke.core.VKEngine;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWCharCallback;
import org.lwjgl.glfw.GLFWKeyCallback;

import java.util.HashSet;
import java.util.concurrent.CopyOnWriteArrayList;

public class KeyboardInput implements Framable, KeyListener {
    private static final int KEY_AMOUNT = Key.values().length;
    private final SingleKeyState[] keyStates;

    private final CopyOnWriteArrayList<KeyListener> listeners;

    private final HashSet<KeyStroke> keyStrokes;


    public KeyboardInput(VKEngine engine) {
        this.keyStates = new SingleKeyState[KEY_AMOUNT];
        for (int i = 0; i < this.keyStates.length; i++) {
            this.keyStates[i] = new SingleKeyState();
        }
        this.listeners = new CopyOnWriteArrayList<>();

        this.keyStrokes = new HashSet<>();

        long window = engine.getWindow().getHandle();
        GLFW.glfwSetKeyCallback(window, new GLFWKeyCallback() {
            @Override
            public void invoke(long window, int glfwKey, int scan, int action, int mods) {
                Key key = Key.fromGlfw(glfwKey);
                if (action == GLFW.GLFW_PRESS) {
                    onPress(key);
                } else if (action == GLFW.GLFW_RELEASE) {
                    onRelease(key);
                }
            }
        });

        GLFW.glfwSetCharCallback(window, new GLFWCharCallback() {
            @Override
            public void invoke(long window, int codepointUTF32) {
                onType(codepointUTF32);
            }
        });
    }

    @Override
    public void preFrame() {
        //i cba to make a dirty cache bro
        for (SingleKeyState keyState : keyStates) {
            keyState.onFrame();
        }
        for (KeyStroke stroke : keyStrokes) {
            stroke.onFrame();
        }
    }

    public InputKeyState key(Key key) {
        return keyStates[key.ordinal()];
    }

    public void registerListener(KeyListener listener) {
        listeners.add(listener);
    }

    public void removeListener(KeyListener listener) {
        listeners.remove(listener);
    }

    @Override
    public void onType(int codepointUTF32) {
        listeners.forEach(l -> l.onType(codepointUTF32));
    }

    @Override
    public void onPress(Key key) {
        listeners.forEach(l -> l.onPress(key));
        this.key(key).requestState(InputKeyState.RequestableState.JustPressed);
    }

    @Override
    public void onRelease(Key key) {
        listeners.forEach(l -> l.onRelease(key));
        this.key(key).requestState(InputKeyState.RequestableState.JustReleased);
    }

    public KeyStroke keyStroke(Key... keys) {
        KeyStroke stroke = new KeyStroke(this, keys);
        this.keyStrokes.add(stroke);
        return stroke;
    }
}
