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
import org.joml.Vector3f;

public class PoopWASDController implements CameraController, Framable {
    private static final float DEFAULT_SPEED = 5;

    private final Context context;
    private final PressableState w, a, s, d;

    private float speed;
    private Camera camera;

    public PoopWASDController(Context context) {
        this(context, DEFAULT_SPEED);
    }

    public PoopWASDController(Context context, float speed) {
        this.context = context;
        this.speed = speed;

        InputManager input = context.service(Services.INPUT_MANAGER);
        KeyboardInput keyboard = input.keyboard();
        this.w = keyboard.key(Key.W);
        this.a = keyboard.key(Key.A);
        this.s = keyboard.key(Key.S);
        this.d = keyboard.key(Key.D);

        MouseInput mouse = input.mouse();
        MousePositionState position = mouse.position();
        int x = position.getX();
        int y = position.getY();
    }

    @Override
    public void preFrame() {
        if (camera != null) {
            Vector3f position = camera.position();
            boolean wasModified = false;
            if (w.isPressed()) {
                 position.add(0, 0, -speed);
                 wasModified = true;
            }
            if (d.isPressed()) {
                position.add(speed, 0, 0);
                wasModified = true;
            }
            if (s.isPressed()) {
                position.add(0, 0, speed);
                wasModified = true;
            }
            if (a.isPressed()) {
                position.add(-speed, 0, 0);
                wasModified = true;
            }
            if (wasModified) {
                camera.setPosition(position);
            }
        }
    }

    @Override
    public void attachCamera(Camera camera) {
        this.camera = camera;
        context.getEngine().registerFramable(this);
    }

    @Override
    public void detachCamera() {
        this.camera = null;
        context.getEngine().removeFramable(this);
    }
}
