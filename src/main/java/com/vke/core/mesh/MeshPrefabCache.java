package com.vke.core.mesh;

import com.vke.api.assets.Protocols;
import com.vke.core.assets.AssetException;
import com.vke.core.assets.pipeline.PipelineContext;
import com.vke.core.assets.pipeline.StageElement;
import com.vke.core.assets.pipeline.apis.AssetCache;
import com.vke.core.assets.pipeline.apis.AssetData;
import com.vke.utils.io.Identifier;
import org.jetbrains.annotations.Nullable;

public class MeshPrefabCache implements AssetCache {

    @Override
    public String getTargetProtocol() {
        return Protocols.MESHPREFAB;
    }

    @Override
    public @Nullable AssetData checkCache(PipelineContext context, Identifier assetName) throws AssetException {
        System.out.println("Trying to find cached asset " + assetName);
        return null;
    }

    @Override
    public void cacheElement(PipelineContext context, StageElement element, Identifier assetName) throws AssetException {
        System.out.println("Caching asset " + assetName);
    }
}
