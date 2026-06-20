package com.vke.core.scene.service;

import com.vke.api.app.CompoundFramable;
import com.vke.api.app.Framable;
import com.vke.core.assets.service.AssetManager;
import com.vke.api.scene.Scene;
import com.vke.api.scene.SceneException;
import com.vke.core.Context;
import com.vke.core.services2.Services;
import com.vke.utils.io.Identifier;
import com.vke.utils.iter.Iter;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class SceneManagerScopedImpl implements CompoundFramable, SceneManager {
    private boolean init;
    private final Context context;
    private final SceneManagerBaseImpl base;

    public SceneManagerScopedImpl(Context context, SceneManagerBaseImpl base) {
        this.context = context;
        this.base = base;
    }

    @Override
    public void initialize() {
        if (init) return;
        init = true;
        AssetManager manager = context.service(Services.ASSET_MANAGER);
        manager.initAssets();
        Identifier sceneDirectory = context.id("scenes/");
        base.registerScenes(sceneDirectory, context);
    }

    @Override
    public void setScene(Identifier name) throws SceneException {
        base.setScene(name);
    }

    @Override
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

    @Override
    public String getId() {
        return base.getId();
    }

    @Override
    public List<String> dependencies() {
        return base.dependencies();
    }

    @Override
    public void free() {

    }

    @Override
    public @Nullable SceneTransferState createTransferState() {
        return base.createTransferState();
    }

    @Override
    public void applyTransferState(@Nullable SceneTransferState state) {
        base.applyTransferState(state, context);
    }
}
