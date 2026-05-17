package com.vke.api.services2;

import com.vke.core.Context;
import com.vke.core.VKEngine;

import java.util.HashMap;

public abstract class ScopedServiceImpl<T> extends ServiceImpl {
    private final HashMap<String, T> cache;

    public ScopedServiceImpl(String id, VKEngine engine) {
        super(id, engine);
        this.cache = new HashMap<>();
    }

    public T getScoped(Context context) {
        String namespace = context.getName();
        T cached = cache.get(namespace);
        if (cached == null) {
            cached = createScoped(context);
            cache.put(namespace, cached);
        }
        return cached;
    }

    protected abstract T createScoped(Context context);
}
