package com.vke.core.input.service;

import com.vke.api.app.Framable;
import com.vke.api.services2.Service;
import com.vke.core.input.keyboard.KeyboardInput;
import com.vke.core.input.keyboard.KeyboardInputImpl;
import com.vke.core.input.mouse.MouseInput;

public interface InputManager extends Service, Framable {
    KeyboardInput keyboard();

    MouseInput mouse();
}
