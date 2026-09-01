package com.vke.core.assets.meta;

import com.vke.api.assets.AssetMeta;
import com.vke.api.assets.Protocols;
import com.vke.core.Identifier;

public class PartialAssetMeta implements AssetMeta {
    private final Identifier name;

    public PartialAssetMeta(Identifier assetName) {
        this.name = assetName;
    }

    @Override
    public String getProtocol() {
        return Protocols.ANY;
    }

    @Override
    public String getBundleName() {
        return "";
    }

    @Override
    public Identifier getAssetName() {
        return name;
    }
}
