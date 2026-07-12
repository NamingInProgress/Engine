package com.vke.core.assets.handles;

import com.vke.api.assets.AssetMeta;
import com.vke.core.Context;
import com.vke.core.assets.AssetException;
import com.vke.core.assets.pipeline.apis.AssetData;
import com.vke.core.assets.pipeline.apis.AssetProtocol;
import com.vke.core.assets.pipeline.stages.PipelineStage;
import com.vke.utils.io.Disposable;
import com.vke.utils.io.Identifier;

import java.io.IOException;

public class ProtocolAssetHandle<T> extends CacheOnceAssetHandle<T> {
    private final Identifier identifier;
    private final AssetProtocol.Loader protocolLoader;
    private final AssetMeta meta;

    public ProtocolAssetHandle(Identifier identifier, AssetProtocol.Loader protocolLoader, AssetMeta meta) {
        this.identifier = identifier;
        this.protocolLoader = protocolLoader;
        this.meta = meta;
    }

    @Override
    protected T prepareCache(Context context) throws IOException {
        try {
            AssetData data = protocolLoader.load(context, identifier, PipelineStage.ExecutionTarget.All);
            return data.getDataAs();
        } catch (AssetException e) {
            throw new IOException(e);
        }
    }

    @Override
    public void free() {
        if (get() != null && get() instanceof Disposable d) d.free();
        setCache(null);
    }

    @Override
    public AssetMeta getMeta() {
        return meta;
    }
}
