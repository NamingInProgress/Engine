package com.vke.api.rendering.abstraction.debug;

import com.vke.api.event.EventListener;
import com.vke.api.framable.Framable;
import com.vke.api.logger.Logger;
import com.vke.core.logger.LoggerFactory;
import com.vke.core.rendering.debug.service.DebugManager;
import com.vke.utils.io.Disposable;

public abstract class DebugFeature implements Framable, Disposable, EventListener {

    private final String name;
    protected final Logger logger;

    protected DebugFeature(String name) {
        this.name = name;
        this.logger = LoggerFactory.get("VKE-Debug/" + name);
    }

    public String name() {
        return this.name;
    }

}
