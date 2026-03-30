package com.vke.api.assets;

import com.vke.utils.io.Identifier;

public class AssetUnavailableException extends Exception {
    public AssetUnavailableException() {
        super("Asset was not loaded!");
    }

    public AssetUnavailableException(Identifier id) {
        super("Asset was not loaded: " + id);
    }
}
