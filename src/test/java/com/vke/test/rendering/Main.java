package com.vke.test.rendering;

import com.vke.api.window.WindowCreateInfo;
import com.vke.config.ConfigurationOption;
import com.vke.core.EngineCreateInfo;
import com.vke.core.VKEngine;
import com.vke.core.scene.SceneApp;

public class Main {
    public static void main(String[] args) throws InterruptedException {

        EngineCreateInfo createInfo = new EngineCreateInfo("CUBE", "vke");
        createInfo.releaseMode = true;
        //createInfo.vsync = true;
        createInfo.windowCreateInfo = new WindowCreateInfo("Cube test");

        ConfigurationOption<Boolean> renderdoc = new ConfigurationOption<>("renderdoc", ConfigurationOption.Initializer.BOOLEAN);

        VKEngine engine = new VKEngine(createInfo);

        if (renderdoc.get()) Thread.sleep(5000);

        engine.start(new SceneApp("main"));
    }
}
