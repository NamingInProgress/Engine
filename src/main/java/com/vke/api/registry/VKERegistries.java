package com.vke.api.registry;

import com.vke.api.registry.registries.PipelinesRegistry;
import com.vke.api.registry.registries.SerializersRegistry;
import com.vke.api.registry.registries.VKERegistry;
import com.vke.api.serializer.Serializer;
import com.vke.api.vulkan.pipeline.RenderPipeline;
import com.vke.utils.Identifier;

import java.util.LinkedHashMap;
import java.util.Map;

public class VKERegistries {

    private static final Map<String, VKERegistrate> REGISTRATES = new LinkedHashMap<>();

    public static final VKERegistry<Class<?>, Serializer<?>> SERIALIZERS = new SerializersRegistry(new Identifier("vke", "serializers"));
    public static final PipelinesRegistry PIPELINES = new PipelinesRegistry(new Identifier("vke", "pipelines"));

    public static VKERegistrate get(String addonId) {
        return REGISTRATES.computeIfAbsent(addonId, VKERegistrate::new);
    }

}
