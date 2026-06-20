package com.vke.core.assets.service;

import com.vke.api.assets.AssetHandle;
import com.vke.api.assets.BundleExchange;
import com.vke.api.services2.ServiceAPI;
import com.vke.api.services2.ServiceImpl;
import com.vke.core.assets.pipeline.PipelineContext;
import com.vke.core.services2.Services;
import com.vke.utils.io.Identifier;
import com.vke.utils.iter.Iter;
import org.jetbrains.annotations.Nullable;

public class AssetManagerAPI extends ServiceAPI implements AssetManager {
    public AssetManagerAPI(ServiceImpl baseImpl) {
        super(Services.ASSET_MANAGER, baseImpl);
    }

    private AssetManager getImpl() {
        return (AssetManager) getImplementation();
    }

    @Override
    public PipelineContext getPipelineContext() {
        return getImpl().getPipelineContext();
    }

    @Override
    public void initAssets() {
        getImpl().initAssets();
    }

    @Override
    public <T> AssetHandle<T> getAsset(Identifier id) {
        return getImpl().getAsset(id);
    }

    @Override
    public <T> AssetHandle<T> getAsset(String path) {
        return getImpl().getAsset(path);
    }

    @Override
    public Iter<AssetHandle<?>> allAssets() {
        return getImpl().allAssets();
    }

    @Override
    public Iter<AssetHandle<?>> allCurrentlyLoadedAssets() {
        return getImpl().allCurrentlyLoadedAssets();
    }

    @Override
    public BundleExchange beginExchange() {
        return getImpl().beginExchange();
    }
}
