package com.vke.core.assets.meta;

import com.vke.api.assets.AssetMeta;
import com.vke.api.assets.Protocols;
import com.vke.utils.io.Identifier;

public class SpatialAssetMeta implements AssetMeta {
    private final Identifier name;

    public SpatialAssetMeta(Identifier assetName) {
        this.name = assetName;
    }

    @Override
    public String getProtocol() {
        return Protocols.ANY;
    }

    @Override
    public Identifier getAssetName() {
        return name;
    }
}
