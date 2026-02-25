package com.vke.core.services;

import com.vke.core.assets.VKEAssetManager;
import com.vke.core.event.EventBus;
import com.vke.core.services.profiler.Profiler;
import com.vke.core.services.shr.ShaderReflector;
import com.vke.core.vkz.Vkz;
import com.vke.core.vulkan.shader.ShaderCompiler;
import com.vke.core.vulkan.VulkanRenderer;

import static com.vke.api.registry.VKERegistries.SERVICES;

public class Services {

    public static final String VULKAN_RENDERER = "vkr";
    public static final String SHADER_COMPILER = "shc";
    public static final String PROFILER = "prof";
    public static final String EVENT_BUS = "evnt";
    public static final String ASSET_MANAGER = "r";
    public static final String VKZ = "vkz";
    public static final String SHADER_REFLECTION = "shr";

    public static void init() {
        SERVICES.register(VULKAN_RENDERER, (ctx) -> new VulkanRenderer(ctx.engine(), ctx.engineCreateInfo()));
        SERVICES.register(SHADER_COMPILER, (ctx) -> new ShaderCompiler(ctx.engine()));
        SERVICES.register(PROFILER, (_) -> new Profiler());
        SERVICES.register(EVENT_BUS, (ctx) -> new EventBus(ctx.engine()));
        SERVICES.register(ASSET_MANAGER, (ctx) -> new VKEAssetManager(ctx.engine()));
        SERVICES.register(VKZ, (_) -> new Vkz());
        SERVICES.register(SHADER_REFLECTION, (ctx) -> new ShaderReflector(ctx.engine()));
    }

}
