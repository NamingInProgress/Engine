package com.vke.core.scene;

import com.vke.api.app.App;
import com.vke.api.scene.SceneException;
import com.vke.core.VKEngine;
import com.vke.core.scene.manager.SceneManager;
import com.vke.core.services.Services;
import com.vke.core.vulkan.VulkanRenderer;
import com.vke.core.window.Window;

public class SceneApp extends App {
    private VKEngine engine;
    private final String sceneName;

    public SceneApp(String sceneName) {
        this.sceneName = sceneName;
    }

    @Override
    public void onInit(VKEngine engine) {
        this.engine = engine;
        engine.service(Services.VULKAN_RENDERER);
        SceneManager sceneManager = engine.service(Services.SCENE_MANAGER);
        sceneManager.initialize();

        try {
            sceneManager.setScene(sceneName);
        } catch (SceneException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onDraw(Window window, VulkanRenderer.FrameData fd) {
        SceneManager sceneManager = engine.service(Services.SCENE_MANAGER);
        sceneManager.callDrawLoop(window, fd);
    }

    @Override
    public String getName() {
        return sceneName;
    }

    @Override
    public void free() {

    }
}
