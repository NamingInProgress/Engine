package com.vke.core.scene;

import com.vke.api.app.App;
import com.vke.api.app.CompoundFramable;
import com.vke.api.app.Framable;
import com.vke.api.scene.SceneException;
import com.vke.core.VKEngine;
import com.vke.core.scene.service.SceneManager;
import com.vke.core.scene.service.SceneManagerScopedImpl;
import com.vke.core.services2.Services;
import com.vke.utils.iter.Iter;

public class SceneApp extends App implements CompoundFramable {
    private VKEngine engine;
    private final String sceneName;

    public SceneApp(String sceneName) {
        this.sceneName = sceneName;
    }

    @Override
    public void onInit(VKEngine engine) {
        this.engine = engine;
        SceneManager sceneManager = engine.service(Services.SCENE_MANAGER);
        sceneManager.initialize();
        engine.service(Services.VULKAN_RENDERER);

        try {
            sceneManager.setScene(sceneName);
        } catch (SceneException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Iter<Framable> children() {
        return Iter.of(engine.<SceneManagerScopedImpl>service(Services.SCENE_MANAGER));
    }

    @Override
    public String getName() {
        return sceneName;
    }

    @Override
    public void free() {

    }
}
