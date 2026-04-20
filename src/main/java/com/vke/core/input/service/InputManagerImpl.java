package com.vke.core.input.service;

import com.vke.api.services2.ServiceImpl;
import com.vke.core.VKEngine;
import com.vke.core.input.keyboard.KeyboardInputImpl;
import com.vke.core.input.mouse.MouseInputImpl;
import com.vke.core.services2.Services;

import java.util.List;

public class InputManagerImpl extends ServiceImpl implements InputManager {
    private final VKEngine engine;
    private KeyboardInputImpl keyboardInput;
    private MouseInputImpl mouseInput;

    public InputManagerImpl(VKEngine engine) {
        super(Services.INPUT_MANAGER, engine);
        this.engine = engine;
    }

    @Override
    protected void onInitialize() {
        keyboardInput = new KeyboardInputImpl(engine);
        mouseInput = new MouseInputImpl(engine);
        engine.registerFramable(this);
    }

    @Override
    public KeyboardInputImpl keyboard() {
        return keyboardInput;
    }

    @Override
    public MouseInputImpl mouse() {
        return mouseInput;
    }

    @Override
    public void preFrame() {
        keyboardInput.preFrame();
        mouseInput.preFrame();
    }

    @Override
    public List<String> dependencies() {
        return List.of();
    }

    @Override
    public void free() {
        engine.removeFramable(this);
    }
}
