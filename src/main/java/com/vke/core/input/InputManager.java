package com.vke.core.input;

import com.vke.api.app.Framable;
import com.vke.api.services.Service;
import com.vke.core.VKEngine;
import com.vke.core.input.keyboard.KeyboardInput;
import com.vke.core.services.Services;

import java.util.List;

public class InputManager extends Service implements Framable {
    private final VKEngine engine;
    private final KeyboardInput keyboardInput;

    public InputManager(VKEngine engine) {
        super(Services.INPUT_MANAGER);
        this.engine = engine;
        keyboardInput = new KeyboardInput(engine);
        engine.registerFramable(this);
    }

    public KeyboardInput keyboard() {
        return keyboardInput;
    }

    @Override
    public void preFrame() {
        keyboardInput.preFrame();
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
