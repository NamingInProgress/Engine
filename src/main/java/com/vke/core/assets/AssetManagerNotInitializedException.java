package com.vke.core.assets;

public class AssetManagerNotInitializedException extends RuntimeException {
    public AssetManagerNotInitializedException() {
        super("The AssetManager was not initialized by calling initialize() on it!");
    }
}
