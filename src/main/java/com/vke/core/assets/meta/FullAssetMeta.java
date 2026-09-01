package com.vke.core.assets.meta;

import com.vke.core.Identifier;

public class FullAssetMeta implements AttributedAssetMeta {
    private final String protocol;
    private final String bundle;
    private final Identifier assetName;
    private final AssetMetaAttributes attribs;

    public FullAssetMeta(String protocol, String bundle, Identifier assetName, AssetMetaAttributes attribs) {
        this.protocol = protocol;
        this.bundle = bundle;
        this.assetName = assetName;
        this.attribs = attribs;
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

    @Override
    public AssetMetaAttributes getAttributes() {
        return attribs;
    }
}
