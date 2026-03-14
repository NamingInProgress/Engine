package com.vke.core.assets.handles;

import com.vke.api.assets.AssetHandle;
import com.vke.api.language.Language;
import com.vke.api.language.LanguageParser;
import com.vke.api.parsing.config.ConfigDocument;
import com.vke.core.VKEngine;
import com.vke.utils.Identifier;

import java.io.IOException;

public class LanguageAssetHandle extends CacheOnceAssetHandle<Language> {
    private final Identifier identifier;

    public LanguageAssetHandle(Identifier identifier) {
        this.identifier = identifier;
    }

    public LanguageAssetHandle(Language language) {
        this.identifier = null;
        setCache(language);
    }

    @Override
    protected Language prepareCache(VKEngine engine) throws IOException {
        return LanguageParser.parseFromConfig(ConfigDocument.parseIdentifier(identifier));
    }

    @Override
    public Type getType() {
        return Type.Language;
    }

    @Override
    public void free() {
        setCache(null);
    }
}
