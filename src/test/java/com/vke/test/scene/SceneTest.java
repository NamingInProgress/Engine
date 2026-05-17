package com.vke.test.scene;

import com.vke.api.scene.SceneException;
import com.vke.api.window.WindowCreateInfo;
import com.vke.core.Context;
import com.vke.core.EngineCreateInfo;
import com.vke.core.VKEngine;
import com.vke.core.scene.service.SceneManagerScopedImpl;
import com.vke.core.services2.Services;

public class SceneTest {
    public static void main(String[] args) throws SceneException {
        EngineCreateInfo createInfo = new EngineCreateInfo("idfk", "vke");
        createInfo.releaseMode = false;
        createInfo.windowCreateInfo = new WindowCreateInfo("My Window");

        VKEngine engine = new VKEngine(createInfo);

        SceneManagerScopedImpl sceneManager = engine.service(Services.SCENE_MANAGER);
        sceneManager.initialize();

        Context lolContext = engine.createNewContext("lol");
        SceneManagerScopedImpl lolSceneManager = lolContext.service(Services.SCENE_MANAGER);
        lolSceneManager.initialize();
        sceneManager.setScene("main");
    }
}
