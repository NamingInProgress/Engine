package com.vke.api.scene;

import com.vke.api.parsing.config.node.ConfigNode;
import com.vke.api.rendering.abstraction.renderer.RenderSystem;
import com.vke.api.rendering.abstraction.renderer.Renderer;
import com.vke.core.Context;
import com.vke.core.Identifier;
import com.vke.core.rendering.graph.GraphContext;
import com.vke.core.rendering.graph.RenderPassInstance;
import com.vke.utils.io.Disposable;

public abstract class Scene implements Disposable {
    private final Identifier name;
    protected final Context context;
    private LoadingScene loadingScene;
    private final Renderer renderer;
    private final RenderSystem system;
    private Identifier graph;

    public Scene(Identifier name, Context context) {
        this.name = name;
        this.context = context;
        this.loadingScene = LoadingScene.defaultVke();
        this.renderer = context.service(context.getEngine().rendererType().serviceName).assumeImplementation();
        this.system = renderer.renderSystem();
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
    public void onRenderPassFinished(RenderPassInstance prevPass, GraphContext context) {};

    public void onUnload() throws Exception {};

    public LoadingScene getLoadingScene() {
        return loadingScene;
    }

    public void setLoadingScene(LoadingScene loadingScene) {
        this.loadingScene = loadingScene;
    }

    public void acceptConfig(ConfigNode node) {}
}
