package com.vke.core.language.service;

import com.vke.api.assets.AssetHandle;
import com.vke.core.Identifier;
import com.vke.core.assets.service.AssetManager;
import com.vke.api.services2.ScopedServiceImpl;
import com.vke.core.Context;
import com.vke.core.VKEngine;
import com.vke.core.language.Language;
import com.vke.core.services2.Services;

import java.io.IOException;
import java.util.List;

public class LanguageManagerBaseImpl extends ScopedServiceImpl<LanguageManagerScopedImpl> implements LanguageManager {
    private final VKEngine engine;
    private Language currentLanguage;
    private long version;

    public LanguageManagerBaseImpl(VKEngine engine) {
        super(Services.LANGUAGE_MANAGER, engine);
        this.engine = engine;
        this.version = Long.MIN_VALUE + 1;
    }

    @Override
    protected void onInitialize() {

    }

    @Override
    public void changeLanguage(String language) throws IOException {
        changeLanguage(engine.id(language));
    }

    @Override
    public void changeLanguage(Identifier newLanguage) throws IOException {
        AssetManager assetManager = engine.service(Services.ASSET_MANAGER);
        AssetHandle<Language> langHandle = assetManager.getAsset(newLanguage);
        if (langHandle == null) throw new IOException("Cant find asset " + newLanguage);
        currentLanguage = langHandle.acquire(engine);

        this.version++;
    }

    @Override
    public Language getCurrentLanguage() {
        return currentLanguage;
    }

    @Override
    public long getVersion() {
        return version;
    }

    @Override
    public List<String> dependencies() {
        return List.of(Services.ASSET_MANAGER);
    }

    @Override
    public void free() {

    }

    @Override
    protected LanguageManagerScopedImpl createScoped(Context context) {
        return new LanguageManagerScopedImpl(context, this);
    }
}
