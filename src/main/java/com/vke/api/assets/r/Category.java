package com.vke.api.assets.r;

import com.vke.api.assets.AssetHandle;
import com.vke.utils.Identifier;

import java.util.HashMap;

public class Category<T> {
    private final HashMap<Identifier, LazyAssetHandle<T>> cache = new HashMap<>();

    public AssetHandle<T> get(Identifier identifier) {
        if (cache.containsKey(identifier)) return cache.get(identifier);
        LazyAssetHandle<T> entry = new LazyAssetHandle<>(identifier);
        cache.put(identifier, entry);
        return entry;
    }

    public AssetHandle<T> get(String rawIdentifier) {
        //dont use the cache here cuz the namespace could vary i think
        Identifier identifier = new Identifier("\0", rawIdentifier);
        return new LazyAssetHandle<>(identifier);
    }
}
