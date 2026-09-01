package com.vke.core.language;

import com.vke.api.assets.AssetHandle;
import com.vke.api.assets.r.R;
import com.vke.core.Context;
import com.vke.core.Identifier;

import java.io.IOException;

public class AssetString implements Str {
    private final Identifier identifier;
    private AssetHandle<String> handle;

    AssetString(Identifier identifier) {
        this.identifier = identifier;
    }

    @Override
    public String getContents(Context context) {
        if (handle == null) {
            handle = R.strings.get(identifier);
        }

        try {
            return handle.isAvailable() ? handle.get() : handle.acquire(context);
        } catch (IOException e) {
            context.throwException(e, "getContents of " + identifier);
            return null;
        }
    }
}
