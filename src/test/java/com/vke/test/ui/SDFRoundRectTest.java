package com.vke.test.ui;

import com.vke.api.window.WindowCreateInfo;
import com.vke.config.ConfigurationOption;
import com.vke.core.EngineCreateInfo;
import com.vke.core.VKEngine;
import com.vke.core.scene.SceneApp;

public class SDFRoundRectTest {
    public static void main(String[] args) throws InterruptedException {
        EngineCreateInfo createInfo = new EngineCreateInfo("CUBE", "vke");
        createInfo.releaseMode = false;
        createInfo.vulkanCreateInfo.framesInFlight = 1;
        //createInfo.vsync = true;
        createInfo.windowCreateInfo = new WindowCreateInfo("Cube test");

        ConfigurationOption<Boolean> renderdoc = new ConfigurationOption<>("renderdoc", ConfigurationOption.Initializer.BOOLEAN);

        VKEngine engine = new VKEngine(createInfo);

        if (renderdoc.get()) Thread.sleep(5000);

        engine.start(new SceneApp("sdfroundrect"));
    }
}
