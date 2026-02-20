package com.vke.api.assets;

import com.vke.utils.Disposable;
import com.vke.utils.Identifier;

public interface AssetManager extends Disposable {
    <T> AssetHandle<T> getAsset(Identifier id);

    default AssetHandle.Type getTypeOfAsset(Identifier id) {
        AssetHandle<?> handle = getAsset(id);
        if (handle == null) return null;
        return handle.getType();
    }
}
