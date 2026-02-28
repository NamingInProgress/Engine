package com.vke.api.assets.language;

import com.vke.api.assets.AssetManager;
import com.vke.api.services.Service;
import com.vke.core.VKEngine;
import com.vke.core.services.Services;

import java.util.List;

public class LanguageManager extends Service {
    public LanguageManager(VKEngine engine) {
        super(Services.LANGUAGE_MANAGER);

        AssetManager assetManager = engine.service(Services.ASSET_MANAGER);

    }

    @Override
    protected List<String> dependencies() {
        return List.of(Services.ASSET_MANAGER);
    }

    @Override
    public void free() {

    }
}
