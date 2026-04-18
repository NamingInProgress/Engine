package com.vke.test.input;

import com.vke.api.scene.Scene;
import com.vke.core.Context;
import com.vke.core.input.InputManager;
import com.vke.core.input.keyboard.*;
import com.vke.core.rendering.draw.DrawContext;
import com.vke.core.services.Services;
import com.vke.utils.io.Identifier;

public class InputScene extends Scene {
    private KeyboardInput kb;

    public InputScene(Identifier name, Context context) {
        super(name, context);
    }

    @Override
    public void onLoad() {
        InputManager inputManager = context.service(Services.INPUT_MANAGER);
        this.kb = inputManager.keyboard();

        InputKeyState saveStroke = kb.keyStroke(Key.LEFT_CONTROL, Key.S);
        saveStroke.listen(new StateListener(InputKeyState.State.JustPressed, () -> System.out.println("Saved!")));

        InputKeyState otherStroke = kb.keyStroke(Key.LEFT_CONTROL, Key.S, Key.UP);
        otherStroke.listen(new StateListener(InputKeyState.State.JustPressed, () -> System.out.println("Other!")));
    }

    @Override
    public void onDraw(DrawContext ctx) {
        if (kb.key(Key.W).isPressed()) {
            System.out.println("FORWARD!");
        }
    }

    @Override
    public void onUnload() {

    }

    @Override
    public void free() {

    }
}
