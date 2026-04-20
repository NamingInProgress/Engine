package com.vke.core.services2;

import com.vke.core.EngineCreateInfo;
import com.vke.core.VKEngine;
import com.vke.core.assets.language.manager.LanguageManagerService;
import com.vke.core.assets.manager.VKEAssetManagerService;
import com.vke.core.event.EventBus;
import com.vke.core.input.InputManager;
import com.vke.core.services2.shr.ShaderReflector;
import com.vke.core.profiler.service.ProfilerImpl;
import com.vke.core.scene.manager.SceneManagerService;
import com.vke.core.vkz.Vkz;
import com.vke.core.vulkan.service.VulkanRendererAPI;
import com.vke.core.vulkan.shader.service.ShaderCompilerAPI;
import com.vke.core.vulkan.shader.service.ShaderCompilerImpl;
import com.vke.core.vulkan.service.VulkanRenderer;

public class Services {

    public static final String VULKAN_RENDERER = "vkr";
    public static final String SHADER_COMPILER = "shc";
    public static final String PROFILER = "prof";
    public static final String EVENT_BUS = "evnt";
    public static final String ASSET_MANAGER = "r";
    public static final String VKZ = "vkz";
    public static final String SHADER_REFLECTION = "shr";
    public static final String LANGUAGE_MANAGER = "lan";
    public static final String SCENE_MANAGER = "scn";
    public static final String INPUT_MANAGER = "ipm";

    public static void init(ServiceManager manager, VKEngine engine) {
        EngineCreateInfo createInfo = engine.getCreateInfo();
        manager.registerNewService(VULKAN_RENDERER, new VulkanRendererAPI(new VulkanRenderer(engine, createInfo)));
        manager.registerNewService(SHADER_COMPILER, new ShaderCompilerAPI(new ShaderCompilerImpl(engine)));
        manager.registerNewService(PROFILER, new ProfilerImpl());
        manager.registerNewService(EVENT_BUS, new EventBus(engine));
        manager.registerNewService(ASSET_MANAGER, new VKEAssetManagerService(engine));
        manager.registerNewService(VKZ, new Vkz());
        manager.registerNewService(SHADER_REFLECTION, new ShaderReflector(engine));
        manager.registerNewService(LANGUAGE_MANAGER, new LanguageManagerService(engine));
        manager.registerNewService(SCENE_MANAGER, new SceneManagerService(engine));
        manager.registerNewService(INPUT_MANAGER, new InputManager(engine));
    }

}
