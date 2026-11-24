package com.vke.impl.gameobject.controller;

import com.vke.core.Context;
import com.vke.core.game.object.GameObjectTransform;
import com.vke.core.game.object.controller.AbstractGameObjectController;
import com.vke.core.input.PressableState;
import com.vke.core.input.keyboard.Key;
import com.vke.core.input.keyboard.KeyboardInput;
import com.vke.core.input.service.InputManager;
import com.vke.core.services2.Services;

public class PoopWASDController extends AbstractGameObjectController {
    private static final float DEFAULT_SPEED = 5;

    private final PressableState w, a, s, d;

    private float speed;

    public PoopWASDController(Context context) {
        this(context, DEFAULT_SPEED);
    }

    public PoopWASDController(Context context, float speed) {
        super(context);
        this.context = context;
        this.speed = speed;

        InputManager input = context.service(Services.INPUT_MANAGER);
        KeyboardInput keyboard = input.keyboard();
        this.w = keyboard.key(Key.W);
        this.a = keyboard.key(Key.A);
        this.s = keyboard.key(Key.S);
        this.d = keyboard.key(Key.D);
    }

    @Override
    public void preFrame() {
        GameObjectTransform transform = gameObject.getTransform();
        if (w.isPressed()) {
            transform.changeZ(-speed);
        }
        if (d.isPressed()) {
            transform.changeX(speed);
        }
        if (a.isPressed()) {
            transform.changeX(-speed);
        }
        if (s.isPressed()) {
            transform.changeZ(speed);
        }
    }
}
