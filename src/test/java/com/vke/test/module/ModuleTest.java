package com.vke.test.module;

import com.vke.api.window.WindowCreateInfo;
import com.vke.core.EngineCreateInfo;
import com.vke.core.VKEngine;
import com.vke.core.scene.SceneApp;

public class ModuleTest {
    public static void main(String[] args) {
        EngineCreateInfo createInfo = new EngineCreateInfo("ModulesTest", "vke");
        createInfo.releaseMode = false;
        //createInfo.vsync = true;
        createInfo.windowCreateInfo = new WindowCreateInfo("Modules test");

        VKEngine engine = new VKEngine(createInfo);

        engine.start(new SceneApp("modules"));
    }
}
