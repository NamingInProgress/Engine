package com.vke.core.game.camera.controllers;

import com.vke.api.app.Framable;
import com.vke.api.game.camera.Camera;
import com.vke.api.game.camera.CameraController;
import com.vke.core.Context;
import com.vke.core.input.PressableState;
import com.vke.core.input.keyboard.Key;
import com.vke.core.input.keyboard.KeyboardInput;
import com.vke.core.input.mouse.MouseInput;
import com.vke.core.input.mouse.MousePositionState;
import com.vke.core.input.service.InputManager;
import com.vke.core.services2.Services;
import com.vke.core.window.Window;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class FreecamController implements CameraController, Framable {

    private static final float DEFAULT_SPEED = 0.15f;
    private static final float DEFAULT_SENSITIVITY = 0.0025f;
    private static final float MAX_PITCH = (float) Math.toRadians(89);

    private final Context context;

    private final PressableState w;
    private final PressableState a;
    private final PressableState s;
    private final PressableState d;
    private final PressableState space;
    private final PressableState shift;

    private final MousePositionState mousePosition;

    private int lastX, lastY;

    private final float speed;
    private final float sensitivity;

    private Camera camera;

    private float yaw = 0;
    private float pitch = 0;

    private int centerX;
    private int centerY;

    public FreecamController(Context context) {
        this(context, DEFAULT_SPEED, DEFAULT_SENSITIVITY);
    }

    public FreecamController(Context context, float speed, float sensitivity) {
        this.context = context;
        this.speed = speed;
        this.sensitivity = sensitivity;

        InputManager input = context.service(Services.INPUT_MANAGER);

        KeyboardInput keyboard = input.keyboard();
        w = keyboard.key(Key.W);
        a = keyboard.key(Key.A);
        s = keyboard.key(Key.S);
        d = keyboard.key(Key.D);
        space = keyboard.key(Key.SPACE);
        shift = keyboard.key(Key.LEFT_SHIFT);

        MouseInput mouse = input.mouse();
        mousePosition = mouse.position();

        Window window = context.getEngine().getWindow();
        Window.WindowSize size = window.getSize();
        centerX = size.width() / 2;
        centerY = size.height() / 2;
    }

    @Override
    public void preFrame() {
        if (camera == null) {
            return;
        }

        Window window = context.getEngine().getWindow();

        Window.WindowSize size = window.getSize();
        centerX = size.width() / 2;
        centerY = size.height() / 2;

        int dx = mousePosition.getX() - lastX;
        int dy = mousePosition.getY() - lastY;

        yaw -= dx * sensitivity;
        pitch += dy * sensitivity;

        if (pitch > MAX_PITCH) pitch = MAX_PITCH;
        if (pitch < -MAX_PITCH) pitch = -MAX_PITCH;

        Quaternionf rotation = new Quaternionf()
                .rotateY(yaw)
                .rotateX(pitch);

        camera.setRotation(rotation);

        Vector3f forward = new Vector3f(
                (float) (Math.sin(yaw) * Math.cos(pitch)),
                (float) Math.sin(pitch),
                (float) (-Math.cos(yaw) * Math.cos(pitch))
        ).normalize();

        Vector3f right = new Vector3f(forward)
                .cross(0, 1, 0)
                .normalize();

        Vector3f up = new Vector3f(right)
                .cross(forward)
                .normalize();

        Vector3f position = new Vector3f(camera.position());

        if (w.isPressed()) position.fma(speed, forward);
        if (s.isPressed()) position.fma(-speed, forward);
        if (d.isPressed()) position.fma(speed, right);
        if (a.isPressed()) position.fma(-speed, right);
        if (space.isPressed()) position.fma(speed, up);
        if (shift.isPressed()) position.fma(-speed, up);

        camera.setPosition(position);

        lastX = mousePosition.getX();
        lastY = mousePosition.getY();

    }

    @Override
    public void attachCamera(Camera camera) {
        this.camera = camera;

        Window window = context.getEngine().getWindow();
        Window.WindowSize size = window.getSize();
        centerX = size.width() / 2;
        centerY = size.height() / 2;

        context.getEngine().registerFramable(this);

        window.disableCursor();
    }

    @Override
    public void detachCamera() {
        camera = null;
        context.getEngine().removeFramable(this);
    }
}