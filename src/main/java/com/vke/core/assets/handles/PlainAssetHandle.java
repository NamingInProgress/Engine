package com.vke.core.assets.handles;

import com.vke.core.VKEngine;
import com.vke.utils.Identifier;
import com.vke.utils.Utils;

import java.io.IOException;

public class PlainAssetHandle extends CacheOnceAssetHandle<String> {
    private final Identifier identifier;

    public PlainAssetHandle(Identifier identifier) {
        this.identifier = identifier;
    }

    @Override
    protected String prepareCache(VKEngine engine) throws IOException {
        return Utils.readStringFromInputStream(identifier.asInputStream());
    }

    @Override
    public Type getType() {
        return Type.String;
    }

    @Override
    public void free() {
        setCache(null);
    }
}
