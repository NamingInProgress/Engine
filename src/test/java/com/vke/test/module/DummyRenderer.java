package com.vke.test.module;

import com.vke.api.rendering.FrameCounter;
import com.vke.api.rendering.abstraction.RenderDevice;
import com.vke.api.rendering.abstraction.Renderer;
import com.vke.api.rendering.abstraction.data.ITextureManager;
import com.vke.api.services2.ServiceImpl;
import com.vke.core.VKEngine;
import com.vke.core.services2.Services;

import java.util.List;

public class DummyRenderer extends ServiceImpl implements Renderer {
    public DummyRenderer(VKEngine engine) {
        super(Services.VULKAN_RENDERER, engine);
    }

    @Override
    public RenderDevice getDevice() {
        return null;
    }

    @Override
    public FrameCounter getFrameCounter() {
        return null;
    }

    @Override
    public ITextureManager textureManager() {
        return null;
    }

    @Override
    protected void onInitialize() {

    }

    @Override
    public List<String> dependencies() {
        return List.of();
    }

    @Override
    public void free() {

    }
}
