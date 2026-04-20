package com.vke.core.input;

import com.vke.api.app.Framable;
import com.vke.api.services.Service;
import com.vke.core.VKEngine;
import com.vke.core.input.keyboard.KeyboardInput;
import com.vke.core.input.mouse.MouseInput;
import com.vke.core.services2.Services;

import java.util.List;

public class InputManager extends Service implements Framable {
    private final VKEngine engine;
    private final KeyboardInput keyboardInput;
    private final MouseInput mouseInput;

    public InputManager(VKEngine engine) {
        super(Services.INPUT_MANAGER);
        this.engine = engine;
        keyboardInput = new KeyboardInput(engine);
        mouseInput = new MouseInput(engine);
        engine.registerFramable(this);
    }

    public KeyboardInput keyboard() {
        return keyboardInput;
    }

    public MouseInput mouse() {
        return mouseInput;
    }

    @Override
    public void preFrame() {
        keyboardInput.preFrame();
        mouseInput.preFrame();
    }

    @Override
    protected List<String> dependencies() {
        return List.of();
    }

    @Override
    public void free() {
        engine.removeFramable(this);
    }
}
