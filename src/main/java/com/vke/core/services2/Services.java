package com.vke.core.services2;

import com.vke.core.EngineCreateInfo;
import com.vke.core.VKEngine;
import com.vke.core.assets.language.service.LanguageManagerAPI;
import com.vke.core.assets.language.service.LanguageManagerBaseImpl;
import com.vke.core.assets.service.AssetManagerAPI;
import com.vke.core.assets.service.AssetManagerBaseImpl;
import com.vke.core.event.service.EventBusAPI;
import com.vke.core.event.service.EventBusImpl;
import com.vke.core.input.service.InputManagerAPI;
import com.vke.core.input.service.InputManagerImpl;
import com.vke.core.profiler.service.ProfilerAPI;
import com.vke.core.rendering.reflection2.service.ShaderReflector2API;
import com.vke.core.rendering.reflection2.service.ShaderReflector2Impl;
import com.vke.core.scene.service.SceneManagerAPI;
import com.vke.core.vulkan.shr.service.ShaderReflectorAPI;
import com.vke.core.vulkan.shr.service.ShaderReflectorImpl;
import com.vke.core.profiler.service.ProfilerImpl;
import com.vke.core.scene.service.SceneManagerBaseImpl;
import com.vke.core.vkz.service.VkzAPI;
import com.vke.core.vkz.service.VkzImpl;
import com.vke.core.vulkan.service.VulkanRendererAPI;
import com.vke.core.vulkan.shader.service.ShaderCompilerAPI;
import com.vke.core.vulkan.shader.service.ShaderCompilerImpl;
import com.vke.core.vulkan.service.VulkanRenderer;

public class Services {

    public static final String VULKAN_RENDERER = "vkr";
    public static final String SHADER_COMPILER = "shc";
    public static final String PROFILER = "prf";
    public static final String EVENT_BUS = "evt";
    public static final String ASSET_MANAGER = "asm";
    public static final String VKZ = "vkz";
    public static final String SHADER_REFLECTION = "shr";
    public static final String LANGUAGE_MANAGER = "lan";
    public static final String SCENE_MANAGER = "scn";
    public static final String INPUT_MANAGER = "ipm";
    public static final String SHADER_REFLECTION2 = "shr2";

    public static void init(ServiceManager manager, VKEngine engine) {
        EngineCreateInfo createInfo = engine.getCreateInfo();
        manager.registerNewService(VULKAN_RENDERER, new VulkanRendererAPI(new VulkanRenderer(engine, createInfo)));
        manager.registerNewService(SHADER_COMPILER, new ShaderCompilerAPI(new ShaderCompilerImpl(engine)));
        manager.registerNewService(PROFILER, new ProfilerAPI(new ProfilerImpl(engine)));
        manager.registerNewService(EVENT_BUS, new EventBusAPI(new EventBusImpl(engine)));
        manager.registerNewService(ASSET_MANAGER, new AssetManagerAPI(new AssetManagerBaseImpl(engine)));
        manager.registerNewService(VKZ, new VkzAPI(new VkzImpl(engine)));
        manager.registerNewService(SHADER_REFLECTION, new ShaderReflectorAPI(new ShaderReflectorImpl(engine)));
        manager.registerNewService(LANGUAGE_MANAGER, new LanguageManagerAPI(new LanguageManagerBaseImpl(engine)));
        manager.registerNewService(SCENE_MANAGER, new SceneManagerAPI(new SceneManagerBaseImpl(engine)));
        manager.registerNewService(INPUT_MANAGER, new InputManagerAPI(new InputManagerImpl(engine)));
        manager.registerNewService(SHADER_REFLECTION2, new ShaderReflector2API(new ShaderReflector2Impl(engine)));
    }

}
