package com.vke.test.audio;

import com.vke.api.window.WindowCreateInfo;
import com.vke.config.ConfigurationOption;
import com.vke.core.EngineCreateInfo;
import com.vke.core.VKEngine;
import com.vke.core.scene.SceneApp;

public class AudioLoadTest {
    public static void main(String[] args) throws InterruptedException {
        EngineCreateInfo createInfo = new EngineCreateInfo("InputTest", "vke");
        createInfo.releaseMode = true;
        //createInfo.vsync = true;
        createInfo.windowCreateInfo = new WindowCreateInfo("Input Test");

        ConfigurationOption<Boolean> renderdoc = new ConfigurationOption<>("renderdoc", ConfigurationOption.Initializer.BOOLEAN);

        VKEngine engine = new VKEngine(createInfo);

        if (renderdoc.get()) Thread.sleep(5000);

        engine.start(new SceneApp("audio"));
    }
}