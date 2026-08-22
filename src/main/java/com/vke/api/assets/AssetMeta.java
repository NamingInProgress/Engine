package com.vke.api.assets;

import com.vke.core.Identifier;

public interface AssetMeta {
    String getProtocol();
    String getBundleName();
    Identifier getAssetName();
}
