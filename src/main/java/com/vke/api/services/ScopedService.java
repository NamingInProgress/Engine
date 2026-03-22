package com.vke.api.services;

import com.vke.core.Context;

import java.util.HashMap;

public abstract class ScopedService<T> extends Service {
    private final HashMap<String, T> cache;

    public ScopedService(String id) {
        super(id);
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
