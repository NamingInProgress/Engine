package com.vke.api.assets;

import com.vke.core.assets.pipeline.PipelineContext;
import com.vke.utils.io.Identifier;
import com.vke.utils.iter.Iter;

public interface AssetManager {
    PipelineContext getPipelineContext();

    void initialize();

    <T> AssetHandle<T> getAsset(Identifier id);
    <T> AssetHandle<T> getAsset(String path);

    Iter<AssetHandle<?>> allAssets();
    Iter<AssetHandle<?>> allCurrentlyLoadedAssets();

    BundleExchange beginExchange();

    default String getAssetProtocol(Identifier id) {
        AssetHandle<?> handle = getAsset(id);
        if (handle == null) return Protocols.ANY;
        return handle.getProtocol();
    }
}
