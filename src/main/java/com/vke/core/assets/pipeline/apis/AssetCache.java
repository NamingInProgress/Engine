package com.vke.core.assets.pipeline.apis;

import com.vke.core.assets.AssetException;
import com.vke.core.assets.pipeline.PipelineContext;
import com.vke.core.assets.pipeline.StageElement;
import com.vke.utils.io.Identifier;
import org.jetbrains.annotations.Nullable;

public interface AssetCache {
    String getTargetProtocol();
    @Nullable AssetData checkCache(PipelineContext context, Identifier assetName) throws AssetException;
    void cacheElement(PipelineContext context, StageElement element, Identifier assetName) throws AssetException;
}
