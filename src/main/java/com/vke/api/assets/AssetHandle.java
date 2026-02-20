package com.vke.api.assets;

import com.vke.core.VKEngine;
import com.vke.core.assets.handles.tex.PngTextureHandle;
import com.vke.utils.Disposable;
import com.vke.utils.Identifier;

import java.io.IOException;

public interface AssetHandle<T> extends Disposable {
    enum Type {
        Texture,
        String,
        Bool,
        Number,
        Unresolved
    }

    Type getType();

    /**
     * Returns the underlying asset and obtains it from the AssetManager if it's not available.
     * @return the asset
     */
    T acquire(VKEngine engine) throws IOException;

    /**
     * Tries to get this asset and returns null if it's not available.
     * @return The asset
     */
    T get();

    boolean isAvailable();

    static AssetHandle<?> ofFile(Identifier id) {
        String ext = id.getExtensionLower();

        return switch (ext) {
            case "png" -> new PngTextureHandle(id);
            default -> throw new RuntimeException("Unsupported extensions: " + ext);
        };
    }

}
