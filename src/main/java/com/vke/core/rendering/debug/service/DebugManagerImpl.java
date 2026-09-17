package com.vke.core.rendering.debug.service;

import com.vke.api.event.IEventBus;
import com.vke.api.event.EventListener;
import com.vke.api.framable.CompoundFramable;
import com.vke.api.framable.Framable;
import com.vke.api.logger.Logger;
import com.vke.api.rendering.abstraction.debug.DebugFeature;
import com.vke.api.rendering.abstraction.renderer.RenderSystem;
import com.vke.api.rendering.abstraction.renderer.Renderer;
import com.vke.api.services2.ServiceImpl;
import com.vke.core.VKEngine;
import com.vke.core.framable.service.FramableManager;
import com.vke.core.logger.LoggerFactory;
import com.vke.core.services2.Services;
import com.vke.utils.io.Disposable;
import com.vke.utils.iter.Iter;

import java.util.LinkedList;
import java.util.List;

public class DebugManagerImpl extends ServiceImpl implements CompoundFramable, Disposable, EventListener, DebugManager {

    private static final Logger LOGGER = LoggerFactory.get("VKE-Debug");

    private final RenderSystem sys;
    private final FramableManager fm;
    private final IEventBus eventBus;

    private List<DebugFeature> enabledFeatures = new LinkedList<>();

    public DebugManagerImpl(VKEngine engine) {
        super(Services.DEBUG, engine);
        Renderer renderer = engine.service(Services.RENDERER);

        this.sys = renderer.renderSystem();
        this.fm = sys.service(Services.FRAMABLE_MANAGER);
        this.eventBus = sys.service(Services.EVENT_BUS);
    }

    public void enable(DebugFeature df) {
        if (enabledFeatures.contains(df))
            LOGGER.warn("Debug feature %s is already enabled!", df.name());
        enabledFeatures.add(df);
        eventBus.register(df);
    }

    public void disable(DebugFeature df) {
        if (!enabledFeatures.contains(df))
            LOGGER.warn("Debug feature %s was not enabled and was requested to be disabled!", df.name());
        enabledFeatures.remove(df);
        eventBus.remove(df);
    }

    @Override
    protected void onInitialize() {
        fm.registerFramable(this);
    }

    @Override
    public List<String> dependencies() {
        return List.of();
    }

    @Override
    public void free() {
        fm.removeFramable(this);
        enabledFeatures.forEach(Disposable::free);
    }

    @Override
    public Iter<Framable> children() {
        return Iter.of(enabledFeatures).cast();
    }
}
