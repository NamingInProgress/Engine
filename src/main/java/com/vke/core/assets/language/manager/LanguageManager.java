package com.vke.core.assets.language.manager;

import com.vke.core.Context;
import com.vke.core.assets.language.Language;
import com.vke.utils.io.Identifier;

import java.io.IOException;

public class LanguageManager {
    private final Context context;
    private final LanguageManagerService service;

    public LanguageManager(Context context, LanguageManagerService service) {
        this.context = context;
        this.service = service;
    }

    public void changeLanguage(String language) throws IOException {
        service.changeLanguage(context.id(language));
    }

    public void changeLanguage(Identifier language) throws IOException {
        service.changeLanguage(language);
    }

    public Language getCurrentLanguage() {
        return service.getCurrentLanguage();
    }

    public long getVersion() {
        return service.getVersion();
    }
}
