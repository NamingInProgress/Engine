package com.vke.core.scene.service;

import com.vke.api.scene.Scene;
import com.vke.api.scene.SceneException;
import com.vke.api.services2.ServiceAPI;
import com.vke.api.services2.ServiceImpl;
import com.vke.core.Identifier;
import com.vke.core.services2.Services;

public class SceneManagerAPI extends ServiceAPI implements SceneManager {
    public SceneManagerAPI(ServiceImpl baseImpl) {
        super(Services.SCENE_MANAGER, baseImpl);
    }

    private SceneManager getImpl() {
        return (SceneManager) getImplementation();
    }

    @Override
    public Scene getCurrentScene() {
        return getImpl().getCurrentScene();
    }

    @Override
    public void setScene(Identifier name) throws SceneException {
        getImpl().setScene(name);
    }

    @Override
    public void setScene(String name) throws SceneException {
        getImpl().setScene(name);
    }
}
