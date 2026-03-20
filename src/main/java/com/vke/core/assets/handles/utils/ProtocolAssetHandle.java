package com.vke.core.assets.handles.utils;

import com.vke.core.VKEngine;
import com.vke.core.assets.pipeline.AssetPipelineException;
import com.vke.core.assets.pipeline.apis.AssetData;
import com.vke.core.assets.pipeline.apis.AssetProtocol;
import com.vke.core.assets.pipeline.stages.PipelineStage;
import com.vke.utils.io.Identifier;

import java.io.IOException;

public class ProtocolAssetHandle<T> extends CacheOnceAssetHandle<T> {
    private final String protocol;
    private final Identifier identifier;
    private final AssetProtocol.Loader protocolLoader;

    public ProtocolAssetHandle(String protocol, Identifier identifier, AssetProtocol.Loader protocolLoader) {
        this.protocol = protocol;
        this.identifier = identifier;
        this.protocolLoader = protocolLoader;
    }

    @Override
    protected T prepareCache(VKEngine engine) throws IOException {
        try {
            AssetData data = protocolLoader.load(engine, identifier, PipelineStage.ExecutionTarget.All);
            return data.getDataAs();
        } catch (AssetPipelineException e) {
            throw new IOException(e);
        }
    }

    @Override
    public String getProtocol() {
        return protocol;
    }

    @Override
    public void free() {
        setCache(null);
    }
}
