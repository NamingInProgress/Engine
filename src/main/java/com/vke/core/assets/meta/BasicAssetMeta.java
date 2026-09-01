package com.vke.core.assets.meta;

import com.vke.api.assets.AssetMeta;
import com.vke.core.Identifier;

public class BasicAssetMeta implements AssetMeta {
    private final String protocol;
    private final String bundle;
    private final Identifier assetName;

    public BasicAssetMeta(String protocol, String bundle, Identifier assetName) {
        this.protocol = protocol;
        this.bundle = bundle;
        this.assetName = assetName;
    }

    @Override
    public String getProtocol() {
        return protocol;
    }

    @Override
    public String getBundleName() {
        return bundle;
    }

    @Override
    public Identifier getAssetName() {
        return assetName;
    }
}
