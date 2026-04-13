package com.vke.core.scene.manager;

import com.vke.api.app.CompoundFramable;
import com.vke.api.app.Framable;
import com.vke.api.assets.AssetManager;
import com.vke.api.scene.Scene;
import com.vke.api.scene.SceneException;
import com.vke.core.Context;
import com.vke.core.rendering.draw.DrawContext;
import com.vke.core.services.Services;
import com.vke.utils.io.Identifier;
import com.vke.utils.iter.Iter;

public class SceneManager implements CompoundFramable {
    private boolean init;
    private final Context context;
    private final SceneManagerService base;

    public SceneManager(Context context, SceneManagerService base) {
        this.context = context;
        this.base = base;
    }

    public void initialize() {
        if (init) return;
        init = true;
        AssetManager manager = context.service(Services.ASSET_MANAGER);
        manager.initialize();
        Identifier sceneDirectory = context.id("scenes/");
        base.registerScenes(sceneDirectory, context);
    }

    public void setScene(Identifier name) throws SceneException {
        base.setScene(name);
    }

    public void setScene(String name) throws SceneException {
        base.setScene(context.id(name));
    }

    public Scene getCurrentScene() {
        return base.getCurrentScene();
    }

    @Override
    public Iter<Framable> children() {
        return getCurrentScene() == null ? Iter.of() : Iter.of(getCurrentScene());
    }

}
