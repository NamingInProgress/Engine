package com.vke.core.assets.service;

import com.vke.api.assets.AssetHandle;
import com.vke.api.assets.BundleExchange;
import com.vke.api.assets.Protocols;
import com.vke.api.services2.PinnedService;
import com.vke.api.services2.Service;
import com.vke.core.assets.pipeline.PipelineContext;
import com.vke.utils.io.Identifier;
import com.vke.utils.iter.Iter;

public interface AssetManager extends PinnedService {
    PipelineContext getPipelineContext();

    void initAssets();

    <T> AssetHandle<T> getAsset(Identifier id);
    <T> AssetHandle<T> getAsset(String path);

    Iter<AssetHandle<?>> allAssets();
    Iter<AssetHandle<?>> allCurrentlyLoadedAssets();

    BundleExchange beginExchange();

    default String getAssetProtocol(Identifier id) {
        AssetHandle<?> handle = getAsset(id);
        if (handle == null) return Protocols.ANY;
        return handle.getMeta().getProtocol();
    }
}
