package com.vke.core.assets.manager;

import com.vke.api.assets.AssetHandle;
import com.vke.api.assets.AssetTransaction;
import com.vke.api.assets.Bundle;
import com.vke.api.assets.BundleLoadingCallback;
import com.vke.api.services.ScopedService;
import com.vke.core.Context;
import com.vke.core.VKEngine;
import com.vke.core.assets.pipeline.PipelineContext;
import com.vke.core.services.Services;
import com.vke.core.thread.TaskProcessor;
import com.vke.utils.io.Disposable;
import com.vke.utils.io.Identifier;

import java.util.HashMap;
import java.util.List;

public class VKEAssetManagerService extends ScopedService<VKEAssetManager> {
    private final VKEngine engine;
    private final PipelineContext pipelineContext;
    private final TaskProcessor loadingThread;
    private final BundleLoadingCallback.List loadingCallbacks;
    private final HashMap<String, Bundle> loadedBundles;

    Bundle globalBundle;
    HashMap<String, Bundle> allBundles;

    public VKEAssetManagerService(VKEngine engine) {
        super(Services.ASSET_MANAGER);
        this.engine = engine;
        this.pipelineContext = new PipelineContext(engine);
        this.loadingThread = new TaskProcessor(engine, "AssetLoader");
        this.loadingCallbacks = new BundleLoadingCallback.List();
        this.allBundles = new HashMap<>();
        this.globalBundle = new Bundle(engine);
        this.loadedBundles = new HashMap<>();
    }

    public PipelineContext getPipelineContext() {
        return pipelineContext;
    }

    public AssetTransaction beginTransaction() {
        return null;
    }

    @SuppressWarnings("unchecked")
    public <T> AssetHandle<T> getAsset(Identifier id) {
        for (Bundle bundle : loadedBundles.values()) {
            AssetHandle<?> tried = bundle.getAsset(id);
            if (tried != null) {
                return (AssetHandle<T>) tried;
            }
        }

        return globalBundle.getAsset(id);
    }

    @Override
    protected VKEAssetManager createScoped(Context context) {
        return new VKEAssetManager(this, context);
    }

    @Override
    protected List<String> dependencies() {
        return List.of();
    }

    @Override
    public void free() {
        globalBundle.free();
        allBundles.values().forEach(Disposable::free);
        loadingThread.free();
    }

    public void registerLoadCallback(BundleLoadingCallback callback) {
        loadingCallbacks.registerCallback(callback);
    }

    public void removeLoadCallback(BundleLoadingCallback callback) {
        loadingCallbacks.removeCallback(callback);
    }
}
