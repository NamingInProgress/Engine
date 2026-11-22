package com.vke.api.scene;

import com.vke.api.parsing.config.node.ConfigNode;
import com.vke.api.rendering.abstraction.renderer.RenderSystem;
import com.vke.api.rendering.abstraction.renderer.Renderer;
import com.vke.core.Context;
import com.vke.core.Identifier;
import com.vke.core.rendering.graph2.GraphContext;
import com.vke.core.rendering.graph2.renderpass.RenderPass;
import com.vke.core.scene.env.SceneEnvironment;
import com.vke.core.services2.Services;
import com.vke.utils.io.Disposable;

public abstract class Scene implements Disposable {
    private final Identifier name;
    protected final Context context;
    private LoadingScene loadingScene;
    private final Renderer renderer;
    private final RenderSystem system;
    private Identifier graph;

    private final SceneEnvironment env;

    public Scene(Identifier name, Context context) {
        this.name = name;
        this.context = context;
        this.loadingScene = LoadingScene.defaultVke();
        this.renderer = context.service(Services.RENDERER);
        this.system = renderer.renderSystem();
        this.env = new SceneEnvironment(context);
    }

    public RenderSystem getRenderSystem() {
        return system;
    }

    public Identifier getGraph() { return this.graph; }

    public void setGraph(Identifier graph) {
        this.graph = graph;
    }

    public Renderer getRenderer() { return this.renderer; }

    public Identifier getName() {
        return name;
    }

    public void onLoad() throws Exception {};

    public void onPrepareRendering(GraphContext context) {};
    public void onRenderPassFinished(RenderPass prevPass, GraphContext context) {};

    public void onUnload() throws Exception {};

    public LoadingScene getLoadingScene() {
        return loadingScene;
    }

    public void setLoadingScene(LoadingScene loadingScene) {
        this.loadingScene = loadingScene;
    }

    public void acceptConfig(ConfigNode node) {}

    public SceneEnvironment getEnvironment() {
        return env;
    }

    @Override
    public void free() {
        env.free();
    }
}
