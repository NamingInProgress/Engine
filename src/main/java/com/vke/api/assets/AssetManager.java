package com.vke.api.assets;

import com.vke.core.assets.pipeline.PipelineContext;
import com.vke.utils.io.Disposable;
import com.vke.utils.io.Identifier;

public interface AssetManager {
    PipelineContext getPipelineContext();

    void initialize();

    <T> AssetHandle<T> getAsset(Identifier id);
    <T> AssetHandle<T> getAsset(String path);

    default String getAssetProtocol(Identifier id) {
        AssetHandle<?> handle = getAsset(id);
        if (handle == null) return Protocols.ANY;
        return handle.getProtocol();
    }
}
