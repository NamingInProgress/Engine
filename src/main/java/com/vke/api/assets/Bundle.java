package com.vke.api.assets;

import com.vke.core.VKEngine;
import com.vke.utils.io.Disposable;
import com.vke.utils.io.Identifier;

import java.util.HashMap;

public final class Bundle implements Disposable {
    private final VKEngine engine;

    private final HashMap<Identifier, AssetHandle<?>> assets = new HashMap<>();

    public Bundle(VKEngine engine) {
        this.engine = engine;
    }

    public void extendBundle(Bundle other) {
        this.assets.putAll(other.assets);
    }

    public void addAsset(Identifier identifier, AssetHandle<?> handle) {
        assets.put(identifier, handle);
    }

    @SuppressWarnings("unchecked")
    public <T> AssetHandle<T> getAsset(Identifier id) {
        return (AssetHandle<T>) assets.get(id);
    }

    @SuppressWarnings("unchecked")
    public <T> AssetHandle<T> getAsset(String name) {
        return (AssetHandle<T>) assets.get(engine.id(name));
    }

    @Override
    public void free() {
        assets.values().forEach(Disposable::free);
    }
}
