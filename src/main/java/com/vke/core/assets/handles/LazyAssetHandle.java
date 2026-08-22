package com.vke.core.assets.handles;

import com.vke.api.assets.AssetHandle;
import com.vke.api.assets.AssetMeta;
import com.vke.core.Identifier;
import com.vke.core.assets.meta.PartialAssetMeta;
import com.vke.core.assets.service.AssetManager;
import com.vke.core.Context;
import com.vke.core.services2.Services;

import java.io.IOException;

public class LazyAssetHandle<T> implements AssetHandle<T> {
    private AssetMeta backupMeta;
    private Identifier identifier;
    private AssetHandle<T> cache;

    public LazyAssetHandle(Identifier identifier) {
        this.backupMeta = new PartialAssetMeta(identifier);
        this.identifier = identifier;
        this.cache = null;
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
            backupMeta = new PartialAssetMeta(identifier);
        }
        AssetManager manager = context.service(Services.ASSET_MANAGER);
        AssetHandle<T> assetHandle = manager.getAsset(identifier);
        if (assetHandle == null) {
            throw new IOException(String.format("Tried to get asset handle for \"%s\", but it doesnt exist!", identifier));
        }
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
    public AssetMeta getMeta() {
        if (cache != null) {
            return cache.getMeta();
        }
        return backupMeta;
    }

    @Override
    public void free() {
        if (cache != null) {
            cache.free();
        }
    }
}
