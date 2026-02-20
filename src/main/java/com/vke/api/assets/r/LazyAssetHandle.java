package com.vke.api.assets.r;

import com.vke.api.assets.AssetHandle;
import com.vke.api.assets.AssetManager;
import com.vke.core.VKEngine;
import com.vke.core.services.Services;
import com.vke.utils.Identifier;

import java.io.IOException;

public class LazyAssetHandle<T> implements AssetHandle<T> {
    private Identifier identifier;
    private AssetHandle<T> cache;

    public LazyAssetHandle(Identifier identifier) {
        this.identifier = identifier;
        this.cache = null;
    }

    @Override
    public Type getType() {
        if (cache != null) {
            return cache.getType();
        }
        return Type.Unresolved;
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
