package com.vke.core.scene.manager;

import com.vke.api.scene.Scene;
import com.vke.core.Context;
import com.vke.utils.io.Identifier;

public class SceneManager {
    private final Context context;
    private final SceneManagerService base;

    public SceneManager(Context context, SceneManagerService base) {
        this.context = context;
        this.base = base;
    }

    public void initialize() {
        Identifier sceneDirectory = context.id("scenes/");
        base.registerScenes(sceneDirectory, context);
    }

    public void setScene(Identifier name) {
        base.setScene(name);
    }

    public void setScene(String name) {
        base.setScene(context.id(name));
    }

    public Scene getCurrentScene() {
        return base.getCurrentScene();
    }
}
