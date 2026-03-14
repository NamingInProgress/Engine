package com.vke.api.language;

import com.vke.api.assets.AssetHandle;
import com.vke.api.assets.r.R;
import com.vke.core.VKEngine;
import com.vke.utils.Identifier;

import java.io.IOException;

public class AssetString implements Str {
    private final Identifier identifier;
    private AssetHandle<String> handle;

    AssetString(Identifier identifier) {
        this.identifier = identifier;
    }

    @Override
    public String getContents(VKEngine engine) {
        if (handle == null) {
            handle = R.strings.get(identifier);
        }

        try {
            return handle.isAvailable() ? handle.get() : handle.acquire(engine);
        } catch (IOException e) {
            engine.throwException(e, "getContents of " + identifier);
            return null;
        }
    }
}
