package com.vke.core.language.service;

import com.vke.core.Context;
import com.vke.core.Identifier;
import com.vke.core.language.Language;

import java.io.IOException;
import java.util.List;

public class LanguageManagerScopedImpl implements LanguageManager {
    private final Context context;
    private final LanguageManagerBaseImpl base;

    public LanguageManagerScopedImpl(Context context, LanguageManagerBaseImpl base) {
        this.context = context;
        this.base = base;
    }

    @Override
    public void changeLanguage(String language) throws IOException {
        base.changeLanguage(context.id(language));
    }

    @Override
    public void changeLanguage(Identifier language) throws IOException {
        base.changeLanguage(language);
    }

    @Override
    public Language getCurrentLanguage() {
        return base.getCurrentLanguage();
    }

    @Override
    public long getVersion() {
        return base.getVersion();
    }

    @Override
    public String getId() {
        return base.getId();
    }

    @Override
    public List<String> dependencies() {
        return base.dependencies();
    }

    @Override
    public void free() {

    }
}
