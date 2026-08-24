package com.vke.test.jpeg;

import com.vke.api.window.WindowCreateInfo;
import com.vke.core.EngineCreateInfo;
import com.vke.core.VKEngine;
import com.vke.core.scene.SceneApp;

public class JpegTest {
    public static void main(String[] args) {
        EngineCreateInfo createInfo = new EngineCreateInfo("JPEG", "vke");
        createInfo.releaseMode = true;
        createInfo.vulkanCreateInfo.framesInFlight = 3;
        //createInfo.vsync = true;
        createInfo.windowCreateInfo = new WindowCreateInfo("Jpeg Test");

        VKEngine engine = new VKEngine(createInfo);

        engine.start(new SceneApp("jpeg"));
    }
}
