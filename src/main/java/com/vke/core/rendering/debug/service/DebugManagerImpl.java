package com.vke.core.rendering.debug.service;

import com.vke.api.event.EventListener;
import com.vke.api.framable.Framable;
import com.vke.api.rendering.abstraction.debug.DebugFeature;
import com.vke.api.rendering.abstraction.renderer.RenderSystem;
import com.vke.api.rendering.abstraction.renderer.Renderer;
import com.vke.api.services2.ServiceImpl;
import com.vke.core.VKEngine;
import com.vke.core.framable.service.FramableManager;
import com.vke.core.services2.Services;
import com.vke.utils.io.Disposable;

import java.util.LinkedList;
import java.util.List;

public class DebugManagerImpl extends ServiceImpl implements Framable, Disposable, EventListener, DebugManager {

    private final RenderSystem sys;
    private final FramableManager fm;

    private List<DebugFeature> enabledFeatures = new LinkedList<>();

    public DebugManagerImpl(VKEngine engine) {
        super(Services.DEBUG, engine);

        Renderer renderer = engine.service(Services.RENDERER);

        this.sys = renderer.renderSystem();

        fm = sys.service(Services.FRAMABLE_MANAGER);
        fm.registerFramable(this);
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
