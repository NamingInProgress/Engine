package com.vke.test;

import com.vke.Config;
import com.vke.api.window.WindowCreateInfo;
import com.vke.config.ConfigurationOption;
import com.vke.core.EngineCreateInfo;
import com.vke.core.VKEngine;
import com.vke.core.rendering.vulkan.VulkanRenderer;
import com.vke.core.services.Services;
import com.vke.test.app.TestApp;

public class TestApplication {

    public static void main(String[] args) throws InterruptedException {
        ConfigurationOption<Boolean> renderdoc = new ConfigurationOption<>("renderdoc", ConfigurationOption.Initializer.BOOLEAN);

        if (renderdoc.get()) Thread.sleep(5000);

        EngineCreateInfo createInfo = new EngineCreateInfo();
        createInfo.releaseMode = false;
        createInfo.windowCreateInfo = new WindowCreateInfo("My Window");

        TestPipelines.init();
        VKEngine engine = new VKEngine(createInfo);

        VulkanRenderer renderer = engine.service(Services.VULKAN_RENDERER);

        engine.start(new TestApp());
    }

}
