package com.vke.api.assets.r;

import com.vke.core.Identifier;
import com.vke.core.assets.handles.LazyAssetHandle;

import java.util.HashMap;

public class Category<T> {
    private final HashMap<Identifier, LazyAssetHandle<T>> cache = new HashMap<>();

    public LazyAssetHandle<T> get(Identifier identifier) {
        if (cache.containsKey(identifier)) return cache.get(identifier);
        LazyAssetHandle<T> entry = new LazyAssetHandle<>(identifier);
        cache.put(identifier, entry);
        return entry;
    }

    public LazyAssetHandle<T> get(String name) {
        //dont use the cache here cuz the namespace could vary i think
        Identifier identifier = new Identifier("\0", name);
        return new LazyAssetHandle<>(identifier);
    }
}
