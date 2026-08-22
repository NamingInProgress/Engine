package com.vke.core.assets.handles;

import com.vke.api.assets.AssetMeta;
import com.vke.api.serializer.Loader;
import com.vke.api.serializer.Serializer;
import com.vke.core.Context;
import com.vke.core.FileIdentifier;
import com.vke.core.assets.CacheHandler;
import com.vke.core.assets.pipeline.apis.AssetProtocol;
import com.vke.core.serializer.impl.load.BinaryLoader;
import com.vke.utils.Utils;

import java.io.IOException;

public class LoadFromCacheAssetHandle<T> extends CacheOnceAssetHandle<T> {
    private final FileIdentifier file;
    private final CacheHandler.CacheBlob blob;
    private final AssetProtocol<T> protocol;
    private final AssetMeta meta;

    public LoadFromCacheAssetHandle(FileIdentifier file, CacheHandler.CacheBlob blob, AssetProtocol<T> protocol, AssetMeta meta) {
        this.file = file;
        this.blob = blob;
        this.protocol = protocol;
        this.meta = meta;
    }

    @Override
    protected T prepareCache(Context context) throws IOException {
        return Utils.chainExceptions(() -> {
            Loader loader = new BinaryLoader(file.openInputStream());
            CacheHandler.CacheBlob ignore = Serializer.loadObject(CacheHandler.CacheBlob.class, loader);

            return protocol.deserializeData(loader);
        });
    }

    @Override
    public AssetMeta getMeta() {
        return meta;
    }

    @Override
    public void free() {

    }
}
