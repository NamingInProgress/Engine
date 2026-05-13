package com.vke.test.input;

import com.vke.api.scene.Scene;
import com.vke.core.Context;
import com.vke.core.input.PressableState;
import com.vke.core.input.keyboard.Key;
import com.vke.core.input.keyboard.KeyboardInput;
import com.vke.core.input.keyboard.StateListener;
import com.vke.core.input.mouse.Button;
import com.vke.core.input.mouse.MouseInput;
import com.vke.core.input.mouse.MouseScrollState;
import com.vke.core.input.service.InputManager;
import com.vke.core.rendering.draw.DrawContext;
import com.vke.core.services2.Services;
import com.vke.core.window.Window;
import com.vke.utils.io.Identifier;

public class InputScene extends Scene {
    private KeyboardInput kb;
    private MouseInput mouse;

    public InputScene(Identifier name, Context context) {
        super(name, context);
    }

    @Override
    public void onLoad() {
        Window window = context.getEngine().getWindow();

        InputManager inputManager = context.service(Services.INPUT_MANAGER);
        System.out.println(inputManager);
        this.kb = inputManager.keyboard();
        this.mouse = inputManager.mouse();

        PressableState saveStroke = kb.keyStroke(Key.LEFT_CONTROL, Key.S);
        saveStroke.listen(new StateListener(PressableState.State.JustPressed, () -> System.out.println("Saved!")));

        PressableState otherStroke = kb.keyStroke(Key.LEFT_CONTROL, Key.S, Key.UP);
        otherStroke.listen(new StateListener(PressableState.State.JustPressed, () -> System.out.println("Other!")));

        PressableState leftButton = mouse.button(Button.Left);
        leftButton.listen(new StateListener(PressableState.State.JustPressed, () -> System.out.println("Left mouse!")));

        MouseScrollState scrollState = mouse.scroll();
        //scrollState.listen(new MouseScrollState.Listener() {
        //    @Override
        //    public void onScroll(double dx, double dy, ScrollDirection direction) {
        //        String s = String.format("Scrolled: %f %f -> %s\n", dx, dy, direction);
        //        System.out.println(s);
        //    }
        //});
    }

    @Override
    public void onDraw(DrawContext ctx) {
        if (kb.key(Key.W).isPressed()) {
            System.out.println("FORWARD!");
        }

        System.out.println(mouse.scroll().getDirection());
    }

    @Override
    public void onUnload() {

    }

    @Override
    public void free() {

    }
}
