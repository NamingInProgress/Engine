package com.vke.core;

import com.vke.api.app.Namespace;

public class ModuleContext extends Context {
    private final VKEngine engine;

    public ModuleContext(Namespace namespace, VKEngine engine) {
        super(namespace);
        this.engine = engine;
    }

    @Override
    public VKEngine getEngine() {
        return engine;
    }
}
