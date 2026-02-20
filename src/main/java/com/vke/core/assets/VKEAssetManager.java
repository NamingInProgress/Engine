package com.vke.core.assets;

import com.vke.api.assets.AssetHandle;
import com.vke.api.assets.AssetManager;
import com.vke.api.assets.Bundle;
import com.vke.api.services.Service;
import com.vke.core.VKEngine;
import com.vke.core.event.events.assets.BundleSwapEvent;
import com.vke.core.services.Services;
import com.vke.utils.Disposable;
import com.vke.utils.Identifier;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.List;

public class VKEAssetManager extends Service implements AssetManager {

    private final VKEngine mEngine;

    private Bundle mLoadedBundle;
    private Bundle mGlobalBundleWhichImplementsAssetManager;
    private HashMap<Identifier, Bundle> mAllBundles;

    public VKEAssetManager(VKEngine engine) {
        super(Services.ASSET_MANAGER);
        this.mEngine = engine;
        this.mAllBundles = new HashMap<>();

        this.mGlobalBundleWhichImplementsAssetManager = AssetUtils.collectGlobalBundles(engine);
        AssetUtils.collectBundles(engine, this);
    }

    public void swapBundle(Identifier bundle) {
        Bundle b = mAllBundles.get(bundle);
        if (b == null) {
            mEngine.throwException(new FileNotFoundException(String.format("Bundle '%s' does not exist! Please check your input.", bundle)), "CAssetManager");
        }
        if (!mEngine.EVENT_BUS.fire(new BundleSwapEvent(this.mLoadedBundle, b))) {
            return;
        }
        if (this.mLoadedBundle != null) {
            this.mLoadedBundle.free();
        }
        this.mLoadedBundle = b;
    }

    public void addBundle(Identifier id, Bundle bundle) {
        this.mAllBundles.put(id, bundle);
    }

    public void unloadBundle() {
        if (this.mLoadedBundle != null) {
            this.mLoadedBundle.free();
        }
        this.mLoadedBundle = null;
    }

    @Override
    public <T> AssetHandle<T> getAsset(Identifier id) {
        AssetHandle<T> tried = mLoadedBundle == null ? null : mLoadedBundle.getAsset(id);
        if (tried == null) {
            tried = mGlobalBundleWhichImplementsAssetManager.getAsset(id);
        }
        return tried;
    }

    @Override
    protected List<String> dependencies() {
        return List.of();
    }

    @Override
    public void free() {
        mGlobalBundleWhichImplementsAssetManager.free();
        mAllBundles.values().forEach(Disposable::free);
    }
}
