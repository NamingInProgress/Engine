package com.vke.core.assets.handles;

import com.vke.api.assets.AssetHandle;
import com.vke.core.VKEngine;

import java.io.IOException;

public abstract class CacheOnceAssetHandle<T> implements AssetHandle<T> {
    private T cached;

    protected void setCache(T cached) {
        this.cached = cached;
    }

    public T acquire(VKEngine engine) throws IOException {
        if (isAvailable()) return cached;
        cached = prepareCache(engine);
        return cached;
    }

    protected abstract T prepareCache(VKEngine engine) throws IOException;

    @Override
    public T get() {
        return cached;
    }

    @Override
    public boolean isAvailable() {
        return cached != null;
    }
}
