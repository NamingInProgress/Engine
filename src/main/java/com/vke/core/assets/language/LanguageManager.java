package com.vke.core.assets.language;

import com.vke.api.assets.AssetHandle;
import com.vke.api.assets.AssetManager;
import com.vke.api.services.Service;
import com.vke.core.VKEngine;
import com.vke.core.services.Services;
import com.vke.utils.io.Identifier;

import java.io.IOException;
import java.util.List;

public class LanguageManager extends Service {
    private final VKEngine engine;
    private Language currentLanguage;
    private long version;

    public LanguageManager(VKEngine engine) {
        super(Services.LANGUAGE_MANAGER);
        this.engine = engine;
        this.version = Long.MIN_VALUE + 1;
    }

    public void changeLanguage(Identifier newLanguage) throws IOException {
        AssetManager assetManager = engine.service(Services.ASSET_MANAGER);
        AssetHandle<Language> langHandle = assetManager.getAsset(newLanguage);
        if (langHandle == null) throw new IOException("Cant find asset " + newLanguage);
        currentLanguage = langHandle.acquire(engine);

        this.version++;
    }

    public Language getCurrentLanguage() {
        return currentLanguage;
    }

    @Override
    protected List<String> dependencies() {
        return List.of(Services.ASSET_MANAGER);
    }

    @Override
    public void free() {

    }

    public long getVersion() {
        return version;
    }
}
