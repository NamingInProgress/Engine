package com.vke;

import com.vke.api.event.EventBus;
import com.vke.api.parsing.config.ConfigParser;
import com.vke.api.parsing.config.schema.SchemaMismatchException;
import com.vke.api.window.WindowCreateInfo;
import com.vke.config.ConfigurationOption;
import com.vke.core.EngineCreateInfo;
import com.vke.core.VKEngine;
import com.vke.core.logger.CoreLogger;
import com.vke.core.logger.LoggerFactory;
import com.vke.core.rendering.vulkan.debug.DebugLogFeature;
import com.vke.core.rendering.vulkan.debug.DebugMemoryTrackingFeature;
import com.vke.core.scene.SceneApp;
import com.vke.core.services2.Services;

import java.io.IOException;

public class Main {

    public static final CoreLogger LOG = LoggerFactory.get("VkEngine");

    public static void main(String[] args) throws InterruptedException, ConfigParser.ConfigParseException, IOException, SchemaMismatchException {
        EngineCreateInfo createInfo = new EngineCreateInfo("CUBE", "vke");
        createInfo.releaseMode = true;
        createInfo.vulkanCreateInfo.framesInFlight = 3;
        //createInfo.vsync = true;
        createInfo.windowCreateInfo = new WindowCreateInfo("BEAR test!");

        ConfigurationOption<Boolean> renderdoc = new ConfigurationOption<>("renderdoc", ConfigurationOption.Initializer.BOOLEAN);

        VKEngine engine = new VKEngine(createInfo);
        EventBus ev = engine.service(Services.EVENT_BUS);
//        ev.register(new DebugLogFeature());
//        ev.register(new DebugMemoryTrackingFeature());
        //engine.PROFILER = engine.service(Services.PROFILER);

        if (renderdoc.get()) Thread.sleep(5000);

        engine.start(new SceneApp("main"));
    }
}