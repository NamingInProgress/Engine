package com.vke.api.scene;

import com.vke.api.app.Framable;
import com.vke.api.rendering.abstraction.LowRenderer;
import com.vke.core.Context;
import com.vke.utils.io.Disposable;
import com.vke.utils.io.Identifier;

public abstract class Scene implements Disposable, Framable {
    private final Identifier name;
    protected final Context context;
    private LoadingScene loadingScene;
    private final LowRenderer renderer;

    public Scene(Identifier name, Context context) {
        this.name = name;
        this.context = context;
        this.loadingScene = LoadingScene.defaultVke();
        this.renderer = context.service(context.getEngine().rendererType().serviceName).assumeImplementation();
    }

    public Identifier getName() {
        return name;
    }

    public void onLoad() throws Exception {};

    public void onUnload() throws Exception {};

    public LoadingScene getLoadingScene() {
        return loadingScene;
    }

    public void setLoadingScene(LoadingScene loadingScene) {
        this.loadingScene = loadingScene;
    }

    public LowRenderer getRenderer() { return this.renderer; }
}
