package com.vke.core.assets.handles;

import com.vke.api.assets.AssetHandle;
import com.vke.api.assets.AssetMeta;
import com.vke.core.Context;

import java.io.IOException;

public class ResolvedAssetHandle<T> implements AssetHandle<T> {
    private final T data;
    private final AssetMeta meta;

    public ResolvedAssetHandle(T data, AssetMeta meta) {
        this.data = data;
        this.meta = meta;
    }

    @Override
    public T acquire(Context context) throws IOException {
        return data;
    }

    @Override
    public T get() {
        return data;
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public AssetMeta getMeta() {
        return meta;
    }

    @Override
    public void free() {

    }
}
