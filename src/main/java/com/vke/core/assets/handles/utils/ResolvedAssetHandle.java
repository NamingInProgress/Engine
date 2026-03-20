package com.vke.core.assets.handles.utils;

import com.vke.api.assets.AssetHandle;
import com.vke.core.VKEngine;

import java.io.IOException;

public class ResolvedAssetHandle<T> implements AssetHandle<T> {
    private final String protocol;
    private final T data;

    public ResolvedAssetHandle(String protocol, T data) {
        this.protocol = protocol;
        this.data = data;
    }

    @Override
    public String getProtocol() {
        return protocol;
    }

    @Override
    public T acquire(VKEngine engine) throws IOException {
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
    public void free() {

    }
}
