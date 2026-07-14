package com.vke.core.window.service;

import com.vke.api.services2.ServiceImpl;
import com.vke.api.window.Window;
import com.vke.api.window.WindowCreateInfo;
import com.vke.core.VKEngine;
import com.vke.core.framable.service.FramableManager;
import com.vke.core.services2.Services;
import com.vke.core.window.GlfwWindow;

import java.util.List;

public class WindowManagerImpl extends ServiceImpl implements WindowManager {
    private FramableManager framableManager;

    public WindowManagerImpl(VKEngine engine) {
        super(Services.WINDOW_MANAGER, engine);
    }

    @Override
    protected void onInitialize() {
        this.framableManager = engine.service(Services.FRAMABLE_MANAGER);
    }

    @Override
    public List<String> dependencies() {
        return List.of(Services.FRAMABLE_MANAGER);
    }

    @Override
    public void free() {

    }

    @Override
    public Window createWindow(WindowCreateInfo createInfo) {
        return new GlfwWindow(engine, createInfo, framableManager);
    }
}
