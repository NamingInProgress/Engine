package com.vke.core.assets.handles.utils;

import com.vke.api.assets.AssetHandle;
import com.vke.core.assets.service.AssetManager;
import com.vke.api.assets.Protocols;
import com.vke.core.Context;
import com.vke.core.services2.Services;
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
    public T acquire(Context context) throws IOException {
        if (cache != null) {
            if (cache.isAvailable()) {
                return cache.get();
            }
            return cache.acquire(context);
        }
        if ("\0".equals(identifier.getNamespace())) {
            identifier = context.id(identifier.getPath());
        }
        AssetManager manager = context.service(Services.ASSET_MANAGER);
        AssetHandle<T> assetHandle = manager.getAsset(identifier);
        this.cache = assetHandle;
        return assetHandle.acquire(context);
    }

    @Override
    public T get() {
        if (cache == null) return null;
        return cache.get();
    }

    public T assume(Context context) {
        if (cache == null) {
            try {
                acquire(context);
            } catch (IOException e) {
                //assumption only :)
                throw new RuntimeException(e);
            }
        }
        return cache.get();
    }

    @Override
    public boolean isAvailable() {
        return cache != null;
    }

    @Override
    public Identifier getAssetName() {
        return identifier;
    }

    @Override
    public void free() {
        if (cache != null) {
            cache.free();
        }
    }
}
