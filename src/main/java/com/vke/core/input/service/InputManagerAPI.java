package com.vke.core.input.service;

import com.vke.api.services2.ServiceAPI;
import com.vke.api.services2.ServiceImpl;
import com.vke.core.input.keyboard.KeyboardInput;
import com.vke.core.input.mouse.MouseInput;
import com.vke.core.services2.Services;

public class InputManagerAPI extends ServiceAPI implements InputManager {
    public InputManagerAPI(ServiceImpl baseImpl) {
        super(Services.INPUT_MANAGER, baseImpl);
    }

    private InputManager getImpl() {
        return (InputManager) getImplementation();
    }

    @Override
    public KeyboardInput keyboard() {
        return getImpl().keyboard();
    }

    @Override
    public MouseInput mouse() {
        return getImpl().mouse();
    }
}
