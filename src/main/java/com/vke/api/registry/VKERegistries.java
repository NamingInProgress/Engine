package com.vke.api.registry;

import com.vke.api.registry.registries.*;
import com.vke.api.serializer.Serializer;
import com.vke.core.Identifier;

import java.util.LinkedHashMap;
import java.util.Map;

public class VKERegistries {

    private static final Map<String, VKERegistrate> REGISTRATES = new LinkedHashMap<>();

    public static final VKERegistry<Class<?>, Serializer<?>> SERIALIZERS = new SerializersRegistry(Identifier.of("serializers"));

    public static VKERegistrate get(String addonId) {
        return REGISTRATES.computeIfAbsent(addonId, VKERegistrate::new);
    }

}
