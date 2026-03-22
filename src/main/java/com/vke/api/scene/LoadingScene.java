package com.vke.api.scene;

import com.vke.api.assets.BundleLoadingCallback;
import com.vke.core.VKEngine;
import com.vke.core.assets.manager.VKEAssetManager;
import com.vke.core.services.Services;
import com.vke.utils.io.Identifier;

public abstract class LoadingScene extends Scene implements BundleLoadingCallback {
    protected final VKEAssetManager assetManager;

    protected LoadingScene(Identifier name, VKEngine engine) {
        super(name, engine);
        this.assetManager = engine.service(Services.ASSET_MANAGER);
    }

    public void loadBundle(String bundleName) {
        assetManager.registerLoadCallback(this);
        assetManager.swapBundle(bundleName);
        assetManager.removeLoadCallback(this);
    }
}
