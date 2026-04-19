package com.vke.core.input.mouse;

import com.vke.api.app.Framable;
import com.vke.core.VKEngine;
import com.vke.core.input.PressableState;
import com.vke.core.window.Window;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWCursorPosCallback;
import org.lwjgl.glfw.GLFWMouseButtonCallback;
import org.lwjgl.glfw.GLFWScrollCallback;

import java.util.concurrent.CopyOnWriteArrayList;

public class MouseInput implements Framable, ButtonListener {
    private final VKEngine engine;
    private final MousePositionState positionState;
    private final MouseScrollState scrollState;
    private final MouseButtonState[] buttonStates;

    private final CopyOnWriteArrayList<ButtonListener> buttonListeners;

    public MouseInput(VKEngine engine) {
        this.engine = engine;
        this.positionState = new MousePositionState();
        this.scrollState = new MouseScrollState();

        Button[] buttons = Button.values();
        this.buttonStates = new MouseButtonState[buttons.length];
        for (int i = 0; i < buttonStates.length; i++) {
            buttonStates[i] = new MouseButtonState();
        }

        this.buttonListeners = new CopyOnWriteArrayList<>();

        Window window = engine.getWindow();
        long windowHandle = window.getHandle();
        GLFW.glfwSetCursorPosCallback(windowHandle, new GLFWCursorPosCallback() {
            @Override
            public void invoke(long hwmd, double x, double y) {
                //we dont support sub-pixel position
                int ix = (int) x;
                int iy = (int) y;
                positionState.setXY(ix, window.getSize().height() - iy);
            }
        });

        GLFW.glfwSetMouseButtonCallback(windowHandle, new GLFWMouseButtonCallback() {
            @Override
            public void invoke(long window, int glfwButton, int action, int mods) {
                Button button = Button.fromGlfw(glfwButton);
                if (action == GLFW.GLFW_PRESS) {
                    onPress(button);
                } else if (action == GLFW.GLFW_RELEASE) {
                    onRelease(button);
                }
            }
        });

        GLFW.glfwSetScrollCallback(windowHandle, new GLFWScrollCallback() {
            @Override
            public void invoke(long window, double dx, double dy) {
                scrollState.setDxDy(dx, dy);
            }
        });
    }

    @Override
    public void preFrame() {
        for (MouseButtonState buttonState : buttonStates) {
            buttonState.onFrame();
        }
        scrollState.onFrame();
    }

    public MousePositionState position() {
        return positionState;
    }

    public MouseScrollState scroll() {
        return scrollState;
    }

    public PressableState button(Button button) {
        return this.buttonStates[button.ordinal()];
    }

    @Override
    public void onPress(Button button) {
        buttonListeners.forEach(l -> l.onPress(button));
        this.button(button).requestState(PressableState.RequestableState.JustPressed);
    }

    @Override
    public void onRelease(Button button) {
        buttonListeners.forEach(l -> l.onRelease(button));
        this.button(button).requestState(PressableState.RequestableState.JustReleased);
    }
}
