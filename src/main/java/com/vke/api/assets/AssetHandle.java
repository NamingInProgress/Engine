package com.vke.api.assets;

import com.vke.core.Context;
import com.vke.core.VKEngine;
import com.vke.core.assets.handles.tex.PngTextureHandle;
import com.vke.utils.io.Disposable;
import com.vke.utils.io.Identifier;

import java.io.IOException;

public interface AssetHandle<T> extends Disposable {
    String getProtocol();

    /**
     * Returns the underlying asset and obtains it from the AssetManager if it's not available.
     * @return the asset
     */
    T acquire(Context context) throws IOException;

    /**
     * Tries to get this asset and returns null if it's not available.
     * @return The asset
     */
    T get();

    boolean isAvailable();
}
