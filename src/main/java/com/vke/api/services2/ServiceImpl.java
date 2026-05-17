package com.vke.api.services2;

import com.vke.core.VKEngine;

public abstract class ServiceImpl implements Lifecycle, Service {
    protected final String id;
    protected final VKEngine engine;
    private volatile boolean initialized;
    private boolean hashBeenFreed;

    public ServiceImpl(String id, VKEngine engine) {
        this.id = id;
        this.engine = engine;
    }

    @Override
    public synchronized void initialize() {
        if (initialized) {
            return;
        }
        onInitialize();
        initialized = true;
    }

    @Override
    public boolean isInitialized() {
        return initialized;
    }

    protected abstract void onInitialize();

    @Override
    public String getId() {
        return id;
    }

    public void freeSafe() {
        if (!hashBeenFreed) {
            free();
            hashBeenFreed = true;
        }
    }

    @Override
    public int hashCode() {
        return getId().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ServiceImpl impl) {
            return getId().equals(impl.getId());
        }
        return false;
    }
}
