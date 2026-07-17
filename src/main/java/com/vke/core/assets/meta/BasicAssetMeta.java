package com.vke.core.assets.meta;

import com.vke.api.assets.AssetMeta;
import com.vke.utils.io.Identifier;

public class BasicAssetMeta implements AssetMeta {
    private final String protocol;
    private final Identifier assetName;

    public BasicAssetMeta(String protocol, Identifier assetName) {
        this.protocol = protocol;
        this.assetName = assetName;
    }

    @Override
    public String getProtocol() {
        return protocol;
    }

    @Override
    public Identifier getAssetName() {
        return assetName;
    }
}
