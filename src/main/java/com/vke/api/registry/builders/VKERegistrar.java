package com.vke.api.registry.builders;

public abstract class VKERegistrar<K, V> {

    protected final K key;
    protected boolean registered = false;

    public VKERegistrar(K key) {
        this.key = key;
    }

    public final V register() {
        if (registered) throw new IllegalStateException("Tried to use already used registrar for identifier %s".formatted(key));

        V value = this.build();
        addToRegistry(key, value);
        registered = true;
        return value;
    }

    protected abstract V build();
    protected abstract void addToRegistry(K key, V value);

}
