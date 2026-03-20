package com.vke.core.assets.handles.utils;

import com.vke.api.assets.AssetHandle;
import com.vke.api.assets.AssetManager;
import com.vke.api.assets.Protocols;
import com.vke.core.VKEngine;
import com.vke.core.services.Services;
import com.vke.utils.io.Identifier;

import java.io.IOException;

public class LazyAssetHandle<T> implements AssetHandle<T> {
    private Identifier identifier;
    private AssetHandle<T> cache;

    public LazyAssetHandle(Identifier identifier) {
        this.identifier = identifier;
        this.cache = null;
    }

    @Override
    public String getProtocol() {
        if (cache != null) {
            return cache.getProtocol();
        }
        return Protocols.ANY;
    }

    @Override
    public T acquire(VKEngine engine) throws IOException {
        if (cache != null) {
            if (cache.isAvailable()) {
                return cache.get();
            }
            return cache.acquire(engine);
        }
        if ("\0".equals(identifier.getNamespace())) {
            identifier = engine.id(identifier.getPath());
        }
        AssetManager manager = engine.service(Services.ASSET_MANAGER);
        AssetHandle<T> assetHandle = manager.getAsset(identifier);
        this.cache = assetHandle;
        return assetHandle.acquire(engine);
    }

    @Override
    public T get() {
        if (cache == null) return null;
        return cache.get();
    }

    @Override
    public boolean isAvailable() {
        return cache != null;
    }

    @Override
    public void free() {
        if (cache != null) {
            cache.free();
        }
    }
}
