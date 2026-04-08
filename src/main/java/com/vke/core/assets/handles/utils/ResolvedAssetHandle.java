package com.vke.core.assets.handles.utils;

import com.vke.api.assets.AssetHandle;
import com.vke.core.Context;
import com.vke.core.VKEngine;
import com.vke.utils.io.Identifier;

import java.io.IOException;

public class ResolvedAssetHandle<T> implements AssetHandle<T> {
    private final String protocol;
    private final T data;
    private final Identifier assetName;

    public ResolvedAssetHandle(String protocol, T data, Identifier assetName) {
        this.protocol = protocol;
        this.data = data;
        this.assetName = assetName;
    }

    @Override
    public String getProtocol() {
        return protocol;
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
    public Identifier getAssetName() {
        return assetName;
    }

    @Override
    public void free() {

    }
}
