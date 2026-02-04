package com.vke.core.services;

import com.vke.core.rendering.vulkan.VulkanRenderer;

import static com.vke.api.registry.VKERegistries.SERVICES;

public class Services {

    public static final String VULKAN_RENDERER = "vulkan_renderer";
    public static final String PERFORMANCE_STATISTICS = "perf_stats";

    public static void init() {
        SERVICES.register(VULKAN_RENDERER, (ctx) -> new VulkanRenderer(ctx.engine(), ctx.engineCreateInfo()));
    }

}
