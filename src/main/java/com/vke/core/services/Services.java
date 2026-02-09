package com.vke.core.services;

import com.vke.core.rendering.vulkan.VulkanRenderer;
import com.vke.core.rendering.vulkan.shader.ShaderCompiler;

import static com.vke.api.registry.VKERegistries.SERVICES;

public class Services {

    public static final String VULKAN_RENDERER = "vkr";
    public static final String SHADER_COMPILER = "shc";
    public static final String PERFORMANCE_STATISTICS = "psts";

    public static void init() {
        SERVICES.register(VULKAN_RENDERER, (ctx) -> new VulkanRenderer(ctx.engine(), ctx.engineCreateInfo()));
        SERVICES.register(SHADER_COMPILER, (_) -> new ShaderCompiler());
    }

}
