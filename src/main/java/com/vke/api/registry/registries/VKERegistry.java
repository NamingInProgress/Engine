package com.vke.api.registry.registries;

import com.vke.utils.Identifier;

import java.util.*;

public abstract class VKERegistry<K, V> {

    protected static final Map<Identifier, VKERegistry<?, ?>> REGISTRIES = new HashMap<>();

    protected final Map<K, V> values = new LinkedHashMap<>();

    protected final Identifier registryName;
    protected boolean frozen = false;

    public VKERegistry(Identifier registryName) {
        this.registryName = registryName;

        REGISTRIES.put(registryName, this);
    }

    public static void freezeAll() {
        REGISTRIES.values().forEach(VKERegistry::freeze);
    }

    public static void unfreezeAll() {
        REGISTRIES.values().forEach(VKERegistry::unfreeze);
    }

    public void freeze() {
        if (frozen) throw new IllegalStateException("Registry %s is already frozen!".formatted(registryName));

        this.frozen = true;
    }

    public void unfreeze() {
        if (!frozen) throw new IllegalStateException("Registry %s is already unfrozen!".formatted(registryName));

        this.frozen = false;
    }

    public <T extends V> T register(K key, T value) {
        if (values.containsKey(key)) throw new IllegalStateException("Registry %s already has key %s".formatted(registryName, key));

        if (frozen) throw new IllegalStateException("Tried modifying registry %s while frozen!".formatted(registryName));

        values.put(key, value);

        return value;
    }

    public void clear() {
        if (frozen) throw new IllegalStateException("Tried modifying registry %s while frozen!".formatted(registryName));

        values.clear();
    }

    public V get(K key) { return values.get(key); }
    public V getOrDefault(K key, V def) { return values.getOrDefault(key, def); }

    public K getKey(V value) { return values.keySet().stream().filter((key) -> values.get(key) == value).findFirst().orElse(null); }
    public K getKeyOrDefault(V value, K defaultValue) {
        return values.keySet().stream().filter((key) -> values.get(key) == value).findFirst().orElse(defaultValue);
    }

    public boolean remove(K key) {
        return values.remove(key) != null;
    }

    public Collection<V> values() { return Collections.unmodifiableCollection(this.values.values()); }

    public static class ID<V> extends VKERegistry<Identifier, V> {

        public ID(Identifier registryName) {
            super(registryName);
        }

    }

    public static class String<V> extends VKERegistry<java.lang.String, V> {

        public String(Identifier registryName) {
            super(registryName);
        }

    }

}
