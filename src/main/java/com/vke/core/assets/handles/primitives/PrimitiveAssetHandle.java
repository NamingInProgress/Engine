package com.vke.core.assets.handles.primitives;

import com.vke.api.assets.AssetHandle;
import com.vke.api.assets.AssetUnavailableException;
import com.vke.core.VKEngine;

import java.io.IOException;

public abstract class PrimitiveAssetHandle<T> implements AssetHandle<T> {
    private final T value;
    private final Type type;

    public PrimitiveAssetHandle(T value, Type type) {
        this.value = value;
        this.type = type;
    }

    @Override
    public Type getType() {
        return this.type;
    }

    @Override
    public T acquire(VKEngine engine) throws IOException {
        return value;
    }

    @Override
    public T get() {
        return value;
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public void free() {

    }
}
