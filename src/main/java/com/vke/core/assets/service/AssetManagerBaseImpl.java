package com.vke.core.assets.service;

import com.vke.api.assets.AssetHandle;
import com.vke.api.assets.Bundle;
import com.vke.api.assets.BundleExchange;
import com.vke.api.assets.BundleLoadingCallback;
import com.vke.api.services2.ScopedServiceImpl;
import com.vke.core.Context;
import com.vke.core.VKEngine;
import com.vke.core.assets.AssetException;
import com.vke.core.assets.pipeline.PipelineContext;
import com.vke.core.services2.Services;
import com.vke.core.thread.TaskProcessor;
import com.vke.utils.io.Disposable;
import com.vke.utils.io.Identifier;
import com.vke.utils.iter.Iter;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AssetManagerBaseImpl extends ScopedServiceImpl<AssetManagerScopedImpl> implements AssetManager {
    private final VKEngine engine;
    private PipelineContext pipelineContext;
    private TaskProcessor loadingThread;
    private BundleLoadingCallback.List loadingCallbacks;
    private HashMap<String, Bundle> loadedBundles;
    private Object loadedBundlesLock;

    Bundle globalBundle;
    HashMap<String, Bundle> allBundles;

    public AssetManagerBaseImpl(VKEngine engine) {
        super(Services.ASSET_MANAGER, engine);
        this.engine = engine;
    }

    @Override
    protected void onInitialize() {
        this.pipelineContext = new PipelineContext(engine);
        this.loadingThread = new TaskProcessor(engine, "AssetLoader");
        this.loadingCallbacks = new BundleLoadingCallback.List();
        this.allBundles = new HashMap<>();
        this.globalBundle = new Bundle(engine);
        this.loadedBundles = new HashMap<>();
        this.loadedBundlesLock = new Object();
    }

    public PipelineContext getPipelineContext() {
        return pipelineContext;
    }

    @Override
    public void initAssets() {
        //handled by scoped impl
    }

    public BundleExchange beginExchange() {
        return new BundleExchangeImpl(this);
    }

    void commitExchange(BundleExchangeImpl exchange) throws AssetException {
        synchronized (loadedBundlesLock) {
            loadedBundles.clear();
            HashMap<String, Bundle> removed = new HashMap<>(loadedBundles);
            //in the future replace this with a more fanvy system that keeps certain bundles loaded
            //except they explicitly specify them as heavy or smth

            for (String bundleName : exchange.load) {
                Bundle bundle = allBundles.get(bundleName);
                if (bundle == null) {
                    throw new AssetException("Bundle " + bundleName + " was not found in the AssetManager!");
                }
                loadedBundles.put(bundleName, bundle);
                removed.remove(bundleName);
            }

            for (Bundle removedBundle : removed.values()) {
                removedBundle.free();
            }

            for (Bundle bundle : loadedBundles.values()) {
                loadingThread.addTask(() -> {
                    bundle.preloadAll(loadingCallbacks);
                    loadingCallbacks.onLoadingComplete();
                });
            }
        }
    }

    @SuppressWarnings("unchecked")
    public <T> AssetHandle<T> getAsset(Identifier id) {
        synchronized (loadedBundlesLock) {
            for (Bundle bundle : loadedBundles.values()) {
                AssetHandle<?> tried = bundle.getAsset(id);
                if (tried != null) {
                    return (AssetHandle<T>) tried;
                }
            }

            return globalBundle.getAsset(id);
        }
    }

    @Override
    public <T> AssetHandle<T> getAsset(String path) {
        return getAsset(engine.id(path));
    }

    @Override
    protected AssetManagerScopedImpl createScoped(Context context) {
        return new AssetManagerScopedImpl(this, context);
    }

    @Override
    public List<String> dependencies() {
        return List.of();
    }

    @Override
    public void free() {
        globalBundle.free();
        loadedBundles.values().forEach(Disposable::free);

        synchronized (loadedBundlesLock) {
            loadingThread.free();
        }
    }

    public void registerLoadCallback(BundleLoadingCallback callback) {
        loadingCallbacks.registerCallback(callback);
    }

    public void removeLoadCallback(BundleLoadingCallback callback) {
        loadingCallbacks.removeCallback(callback);
    }

    public void mergeBundles(Map<String, Bundle> bundleMap) {
        bundleMap.forEach((name, bundle) -> {
            Bundle existing = allBundles.get(name);
            if (existing == null) {
                allBundles.put(name, bundle);
            } else {
                existing.extendBundle(bundle);
            }
        });
    }

    public Iter<AssetHandle<?>> allAssets() {
        Iter<AssetHandle<?>> all = globalBundle.allAssets();
        for (Bundle b : allBundles.values()) {
            all = all.chain(b.allAssets());
        }
        return all;
    }

    public Iter<AssetHandle<?>> allCurrentlyLoadedAssets() {
        Iter<AssetHandle<?>> all = globalBundle.allAssets();
        for (Bundle b : loadedBundles.values()) {
            all = all.chain(b.allAssets());
        }
        return all;
    }

    public @Nullable BundleLoadingCallback getCallbacks() {
        return loadingCallbacks;
    }
}
