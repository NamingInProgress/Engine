package com.vke.core.game.camera.controllers;

import com.vke.api.framable.Framable;
import com.vke.api.game.camera.Camera;
import com.vke.api.game.camera.CameraController;
import com.vke.core.Context;
import com.vke.core.framable.service.FramableManager;
import com.vke.core.input.PressableState;
import com.vke.core.input.mouse.Button;
import com.vke.core.input.mouse.MouseInput;
import com.vke.core.input.mouse.MousePositionState;
import com.vke.core.input.mouse.MouseScrollState;
import com.vke.core.input.service.InputManager;
import com.vke.core.services2.Services;
import org.joml.Vector3f;

public class CameraController2DMB implements CameraController, Framable {

    private static final float DEFAULT_SENSITIVITY = 1;
    private static final float DEFAULT_SCROLL_SENSITIVITY = 0.025f;

    private final Context context;
    private final FramableManager framableManager;

    private final PressableState middle;
    private final MousePositionState mousePosition;
    private final MouseScrollState mouseScroll;

    private double lastX, lastY;
    private float sensitivity, scrollSensitivity;

    private Camera camera;

    public CameraController2DMB(Context context) {
        this(context, DEFAULT_SENSITIVITY, DEFAULT_SCROLL_SENSITIVITY);
    }

    public CameraController2DMB(Context context, float sensitivity, float scrollSensitivity) {
        this.context = context;
        this.sensitivity = sensitivity;
        this.scrollSensitivity = scrollSensitivity;
        this.framableManager = context.service(Services.FRAMABLE_MANAGER);

        InputManager input = context.service(Services.INPUT_MANAGER);

        MouseInput mouse = input.mouse();
        middle = mouse.button(Button.Middle);
        mouseScroll = mouse.scroll();
        mousePosition = mouse.position();
    }

    @Override
    public void preFrame() {
        if (camera == null) {
            return;
        }

        double dx = mousePosition.getX() - lastX;
        double dy = mousePosition.getY() - lastY;
        lastX = mousePosition.getX();
        lastY = mousePosition.getY();

        if (middle.isPressed()) {
            camera.setPosition(new Vector3f(camera.position()).sub((float) (dx * sensitivity), (float) (dy * sensitivity), 0));
        }

        camera.setZoom((float) (camera.zoom() + mouseScroll.getDy() * scrollSensitivity));
    }

    @Override
    public void attachCamera(Camera camera) {
        this.camera = camera;
        framableManager.registerFramable(this);
    }

    @Override
    public void detachCamera() {
        this.camera = null;
        framableManager.removeFramable(this);
    }

    public void setSensitivity(float sensitivity) {
        this.sensitivity = sensitivity;
    }
}
