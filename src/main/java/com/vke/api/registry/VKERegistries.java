package com.vke.api.registry;

import com.vke.api.registry.registries.*;
import com.vke.api.serializer.Serializer;
import com.vke.utils.io.Identifier;

import java.util.LinkedHashMap;
import java.util.Map;

public class VKERegistries {

    private static final Map<String, VKERegistrate> REGISTRATES = new LinkedHashMap<>();

    public static final VKERegistry<Class<?>, Serializer<?>> SERIALIZERS = new SerializersRegistry(new Identifier("serializers"));
    public static final PipelinesRegistry PIPELINES = new PipelinesRegistry(new Identifier("pipelines"));
    public static final ServicesRegistry SERVICES = new ServicesRegistry();

    public static VKERegistrate get(String addonId) {
        return REGISTRATES.computeIfAbsent(addonId, VKERegistrate::new);
    }

}
