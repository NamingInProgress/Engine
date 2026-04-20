package com.vke.api.scene;

import com.vke.api.app.Framable;
import com.vke.core.Context;
import com.vke.utils.io.Disposable;
import com.vke.utils.io.Identifier;

public abstract class Scene implements Disposable, Framable {
    private final Identifier name;
    protected final Context context;
    private LoadingScene loadingScene;

    public Scene(Identifier name, Context context) {
        this.name = name;
        this.context = context;
        this.loadingScene = LoadingScene.defaultVke();
    }

    public Identifier getName() {
        return name;
    }

    public abstract void onLoad();

    public abstract void onUnload();

    public LoadingScene getLoadingScene() {
        return loadingScene;
    }

    public void setLoadingScene(LoadingScene loadingScene) {
        this.loadingScene = loadingScene;
    }
}
