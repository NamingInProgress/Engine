package com.vke.core.assets.handles;

import com.vke.api.parsing.config.ConfigDocument;
import com.vke.api.parsing.config.ConfigParser;
import com.vke.core.VKEngine;
import com.vke.utils.Identifier;
import com.vke.utils.Utils;

import java.io.IOException;

public class ConfigAssetHandle extends CacheOnceAssetHandle<ConfigDocument> {
    private final Identifier identifier;

    public ConfigAssetHandle(Identifier identifier) {
        this.identifier = identifier;
    }

    public ConfigAssetHandle(ConfigDocument document) {
        this.identifier = null;
        setCache(document);
    }

    @Override
    protected ConfigDocument prepareCache(VKEngine engine) throws IOException {
        return ConfigDocument.parseIdentifier(identifier);
    }

    @Override
    public Type getType() {
        return Type.Config;
    }

    @Override
    public void free() {
        setCache(null);
    }
}
