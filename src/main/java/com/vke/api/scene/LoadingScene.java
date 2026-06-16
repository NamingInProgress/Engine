package com.vke.api.scene;

import com.vke.api.assets.BundleExchange;
import com.vke.api.assets.BundleLoadingCallback;
import com.vke.core.Context;
import com.vke.core.assets.AssetException;
import com.vke.core.assets.service.AssetManagerScopedImpl;
import com.vke.core.scene.loading.DefaultVkeLoadingScene;
import com.vke.core.services2.Services;
import com.vke.utils.functionalinterface.FaultyRunnable;
import com.vke.utils.io.Identifier;

public abstract class LoadingScene extends Scene implements BundleLoadingCallback {
    protected final AssetManagerScopedImpl assetManager;
    private FaultyRunnable onComplete;

    public LoadingScene(Identifier name, Context context) {
        super(name, context);
        this.assetManager = context.service(Services.ASSET_MANAGER);
    }

    public static LoadingScene defaultVke() {
        return DefaultVkeLoadingScene.getInstance();
    }

    @Override
    public final LoadingScene getLoadingScene() {
        throw new UnsupportedOperationException("LoadingScenes cannot have a loading scene lol!");
    }

    @Override
    public final void setLoadingScene(LoadingScene loadingScene) {
        throw new UnsupportedOperationException("LoadingScenes cannot have a loading scene lol!");
    }

    public void loadBundles(java.util.List<String> bundleNames, FaultyRunnable onComplete) throws Exception {
        onLoad();
        this.onComplete = onComplete;
        assetManager.registerLoadCallback(this);
        BundleExchange exchange = assetManager.beginExchange();
        exchange.loadAll(bundleNames);
        exchange.commit();
    }

    protected void completeLoading() throws Exception {
        onUnload();
        assetManager.removeLoadCallback(this);
        if (onComplete != null) onComplete.run();
    }
}
