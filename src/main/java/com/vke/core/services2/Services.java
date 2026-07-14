package com.vke.core.services2;

import com.vke.core.EngineCreateInfo;
import com.vke.core.VKEngine;
import com.vke.core.assets.language.service.LanguageManagerAPI;
import com.vke.core.assets.language.service.LanguageManagerBaseImpl;
import com.vke.core.assets.service.AssetManagerAPI;
import com.vke.core.assets.service.AssetManagerBaseImpl;
import com.vke.core.audio.playback.service.AudioManagerMasterAPI;
import com.vke.core.audio.playback.service.AudioManagerMasterImpl;
import com.vke.core.audio.playback2d.service.AudioManager2DAPI;
import com.vke.core.audio.playback2d.service.AudioManager2DBaseImpl;
import com.vke.core.audio.playback3d.service.AudioManager3DAPI;
import com.vke.core.audio.playback3d.service.AudioManager3DBaseImpl;
import com.vke.core.event.service.EventBusAPI;
import com.vke.core.event.service.EventBusImpl;
import com.vke.core.framable.service.FramableManagerAPI;
import com.vke.core.framable.service.FramableManagerImpl;
import com.vke.core.input.service.InputManagerAPI;
import com.vke.core.input.service.InputManagerImpl;
import com.vke.core.profiler.service.ProfilerAPI;
import com.vke.core.scene.service.SceneManagerAPI;
import com.vke.core.rendering.vulkan.shr.service.ShaderReflectorAPI;
import com.vke.core.rendering.vulkan.shr.service.ShaderReflectorImpl;
import com.vke.core.profiler.service.ProfilerImpl;
import com.vke.core.scene.service.SceneManagerBaseImpl;
import com.vke.core.vkz.service.VkzAPI;
import com.vke.core.vkz.service.VkzImpl;
import com.vke.core.rendering.vulkan.service.VulkanRendererAPI;
import com.vke.core.rendering.vulkan.shader.service.ShaderCompilerAPI;
import com.vke.core.rendering.vulkan.shader.service.ShaderCompilerImpl;
import com.vke.core.rendering.vulkan.service.VulkanRenderer;
import com.vke.core.window.service.WindowManagerAPI;
import com.vke.core.window.service.WindowManagerImpl;

public class Services {
    public static final String RENDERER = "rnd";
    public static final String SHADER_COMPILER = "shc";
    public static final String PROFILER = "prf";
    public static final String EVENT_BUS = "evt";
    public static final String ASSET_MANAGER = "asm";
    public static final String VKZ = "vkz";
    public static final String SHADER_REFLECTION = "shr";
    public static final String LANGUAGE_MANAGER = "lan";
    public static final String SCENE_MANAGER = "scn";
    public static final String INPUT_MANAGER = "ipm";
    public static final String AUDIO_MANAGER_2D = "au2";
    public static final String AUDIO_MANAGER_3D = "au3";
    public static final String AUDIO_MANAGER_MASTER = "aum";
    public static final String WINDOW_MANAGER = "wnd";
    public static final String FRAMABLE_MANAGER = "frm";

    public static void init(ServiceManager manager, VKEngine engine) {
        EngineCreateInfo createInfo = engine.getCreateInfo();
        manager.registerNewService(RENDERER, new VulkanRendererAPI(new VulkanRenderer(engine, createInfo)));
        manager.registerNewService(SHADER_COMPILER, new ShaderCompilerAPI(new ShaderCompilerImpl(engine)));
        manager.registerNewService(PROFILER, new ProfilerAPI(new ProfilerImpl(engine)));
        manager.registerNewService(EVENT_BUS, new EventBusAPI(new EventBusImpl(engine)));
        manager.registerNewService(ASSET_MANAGER, new AssetManagerAPI(new AssetManagerBaseImpl(engine)));
        manager.registerNewService(VKZ, new VkzAPI(new VkzImpl(engine)));
        manager.registerNewService(SHADER_REFLECTION, new ShaderReflectorAPI(new ShaderReflectorImpl(engine)));
        manager.registerNewService(LANGUAGE_MANAGER, new LanguageManagerAPI(new LanguageManagerBaseImpl(engine)));
        manager.registerNewService(SCENE_MANAGER, new SceneManagerAPI(new SceneManagerBaseImpl(engine)));
        manager.registerNewService(INPUT_MANAGER, new InputManagerAPI(new InputManagerImpl(engine)));
        manager.registerNewService(AUDIO_MANAGER_2D, new AudioManager2DAPI(new AudioManager2DBaseImpl(engine)));
        manager.registerNewService(AUDIO_MANAGER_3D, new AudioManager3DAPI(new AudioManager3DBaseImpl(engine)));
        manager.registerNewService(AUDIO_MANAGER_MASTER, new AudioManagerMasterAPI(new AudioManagerMasterImpl(engine)));
        manager.registerNewService(WINDOW_MANAGER, new WindowManagerAPI(new WindowManagerImpl(engine)));
        manager.registerNewService(FRAMABLE_MANAGER, new FramableManagerAPI(new FramableManagerImpl(engine)));
    }

}
