package com.vke.core.assets.meta;

import com.vke.utils.io.Identifier;

public class FullAssetMeta implements AttributedAssetMeta {
    private final String protocol;
    private final Identifier assetName;
    private final AssetMetaAttributes attribs;

    public FullAssetMeta(String protocol, Identifier assetName, AssetMetaAttributes attribs) {
        this.protocol = protocol;
        this.assetName = assetName;
        this.attribs = attribs;
    }

    @Override
    public String getProtocol() {
        return protocol;
    }

    @Override
    public Identifier getAssetName() {
        return assetName;
    }

    @Override
    public AssetMetaAttributes getAttributes() {
        return attribs;
    }
}
