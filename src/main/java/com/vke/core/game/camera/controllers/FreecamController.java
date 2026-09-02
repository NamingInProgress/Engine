package com.vke.core.game.camera.controllers;

import com.vke.api.window.Window;
import com.vke.core.Context;
import com.vke.core.game.object.GameObject;
import com.vke.core.game.object.GameObjectTransform;
import com.vke.core.game.object.controller.AbstractGameObjectController;
import com.vke.core.input.PressableState;
import com.vke.core.input.keyboard.Key;
import com.vke.core.input.keyboard.KeyboardInput;
import com.vke.core.input.mouse.MouseInput;
import com.vke.core.input.mouse.MousePositionState;
import com.vke.core.input.service.InputManager;
import com.vke.core.services2.Services;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class FreecamController extends AbstractGameObjectController {

    private static final float DEFAULT_SPEED = 0.5f;
    private static final float DEFAULT_SENSITIVITY = 0.0025f;
    private static final float MAX_PITCH = (float) Math.toRadians(89);

    private final PressableState w;
    private final PressableState a;
    private final PressableState s;
    private final PressableState d;
    private final PressableState space;
    private final PressableState shift;

    private final MousePositionState mousePosition;

    private float speed;
    private float sensitivity;

    private float yaw = 0;
    private float pitch = 0;

    private final Window window;

    public FreecamController(Context context) {
        this(context, DEFAULT_SPEED, DEFAULT_SENSITIVITY);
    }

    public FreecamController(Context context, float speed, float sensitivity) {
        super(context);
        this.context = context;
        this.speed = speed;
        this.sensitivity = sensitivity;

        this.window = context.getEngine().getWindow();

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
    }

    @Override
    public void preFrame() {
        GameObjectTransform transform = gameObject.getTransform();

        yaw = (float) (yaw - (mousePosition.getDx() * sensitivity));
        pitch = (float) (pitch + mousePosition.getDy() * sensitivity);

        if (pitch > MAX_PITCH) pitch = MAX_PITCH;
        if (pitch < -MAX_PITCH) pitch = -MAX_PITCH;

        Quaternionf rotation = new Quaternionf()
                .rotateY(yaw)
                .rotateX(pitch);

        transform.setRotation(rotation);

        Vector3f forward = new Vector3f(0, 0, -1).rotate(rotation);
        Vector3f right   = new Vector3f(1, 0, 0).rotate(rotation);
        Vector3f up      = new Vector3f(0, 1, 0).rotate(rotation);

        Vector3f position = transform.getPosition();

        if (w.isPressed())     position.fma(speed, forward);
        if (s.isPressed())     position.fma(-speed, forward);
        if (d.isPressed())     position.fma(speed, right);
        if (a.isPressed())     position.fma(-speed, right);
        if (space.isPressed()) position.fma(speed, up);
        if (shift.isPressed()) position.fma(-speed, up);

        transform.setPosition(position);
    }

    @Override
    public void setAttachedObject(GameObject object) {
        super.setAttachedObject(object);
        if (object != null) {
            window.disableCursor();
        }
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public void setSensitivity(float sensitivity) {
        this.sensitivity = sensitivity;
    }
}