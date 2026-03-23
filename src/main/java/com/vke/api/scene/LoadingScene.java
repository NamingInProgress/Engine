package com.vke.api.scene;

import com.vke.api.assets.BundleLoadingCallback;
import com.vke.core.Context;
import com.vke.core.assets.manager.VKEAssetManager;
import com.vke.core.services.Services;
import com.vke.utils.io.Identifier;

public abstract class LoadingScene extends Scene implements BundleLoadingCallback {
    protected final VKEAssetManager assetManager;

    protected LoadingScene(Identifier name, Context context) {
        super(name, context);
        this.assetManager = context.service(Services.ASSET_MANAGER);
    }


}
