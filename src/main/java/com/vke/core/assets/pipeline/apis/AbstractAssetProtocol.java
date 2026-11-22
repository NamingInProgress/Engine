package com.vke.core.assets.pipeline.apis;

import com.vke.core.assets.AssetException;
import com.vke.core.assets.pipeline.Op;
import com.vke.core.assets.pipeline.StageElement;
import com.vke.core.assets.pipeline.protocols.loader.UnsupportedLoader;

public interface AbstractAssetProtocol<T> extends AssetProtocol<T> {
    @Override
    default AssetData getField(StageElement data, AssetUri uri) throws AssetException {
        throw AssetException.unknownSelector(getProtocolName(), uri.getSelector());
    }

    @Override
    default Loader getLoader() {
        return new UnsupportedLoader(getProtocolName());
    }

    @Override
    default boolean applies(AssetData a, AssetData b, Op op) {
        return false;
    }
}
