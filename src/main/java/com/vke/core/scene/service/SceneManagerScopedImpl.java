package com.vke.core.scene.service;

import com.vke.api.framable.CompoundFramable;
import com.vke.api.framable.Framable;
import com.vke.core.assets.service.AssetManager;
import com.vke.api.scene.Scene;
import com.vke.api.scene.SceneException;
import com.vke.core.Context;
import com.vke.core.rendering.graph.service.GraphManager;
import com.vke.core.services2.Services;
import com.vke.utils.io.Identifier;
import com.vke.utils.iter.Iter;

import java.util.List;

public class SceneManagerScopedImpl implements SceneManager {
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

        GraphManager graphs = context.service(Services.GRAPH_MANAGER);
        graphs.initialize();
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
}
