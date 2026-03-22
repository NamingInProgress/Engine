package com.vke.core.assets.manager;

import com.vke.api.assets.AssetHandle;
import com.vke.api.assets.Bundle;
import com.vke.api.assets.BundleLoadingCallback;
import com.vke.api.services.ScopedService;
import com.vke.core.Context;
import com.vke.core.VKEngine;
import com.vke.core.assets.pipeline.PipelineContext;
import com.vke.core.event.events.assets.BundleSwapEvent;
import com.vke.core.services.Services;
import com.vke.core.thread.TaskProcessor;
import com.vke.utils.io.Disposable;
import com.vke.utils.io.Identifier;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.List;

public class VKEAssetManagerService extends ScopedService<VKEAssetManager> {
    private final VKEngine engine;
    private final PipelineContext pipelineContext;
    private final TaskProcessor loadingThread;
    private final BundleLoadingCallback.List loadingCallbacks;

    private Bundle loadedBundle;
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
    }

    public PipelineContext getPipelineContext() {
        return pipelineContext;
    }

    public void swapBundle(String bundle) {
        Bundle b = allBundles.get(bundle);
        if (b == null) {
            engine.throwException(new FileNotFoundException(String.format("Bundle '%s' does not exist! Please check your input.", bundle)), "CAssetManager");
            return;
        }
        if (!engine.EVENT_BUS.fire(new BundleSwapEvent(this.loadedBundle, b))) {
            return;
        }
        unloadBundle();
        this.loadingThread.addTask(() -> {
            b.preloadAll(loadingCallbacks);
        });
        this.loadedBundle = b;
    }

    public void unloadBundle() {
        if (this.loadedBundle != null) {
            this.loadedBundle.free();
        }
        this.loadedBundle = null;
    }

    public <T> AssetHandle<T> getAsset(Identifier id) {
        AssetHandle<T> tried = loadedBundle == null ? null : loadedBundle.getAsset(id);
        if (tried == null) {
            tried = globalBundle.getAsset(id);
        }
        return tried;
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
