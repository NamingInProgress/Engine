package com.vke.core.language.service;

import com.vke.api.services2.ServiceAPI;
import com.vke.api.services2.ServiceImpl;
import com.vke.core.Identifier;
import com.vke.core.language.Language;
import com.vke.core.services2.Services;

import java.io.IOException;

public class LanguageManagerAPI extends ServiceAPI implements LanguageManager {
    public LanguageManagerAPI(ServiceImpl baseImpl) {
        super(Services.LANGUAGE_MANAGER, baseImpl);
    }

    private LanguageManager getImpl() {
        return (LanguageManager) getImplementation();
    }

    @Override
    public void changeLanguage(String language) throws IOException {
        getImpl().changeLanguage(language);
    }

    @Override
    public void changeLanguage(Identifier newLanguage) throws IOException {
        getImpl().changeLanguage(newLanguage);
    }

    @Override
    public Language getCurrentLanguage() {
        return getImpl().getCurrentLanguage();
    }

    @Override
    public long getVersion() {
        return getImpl().getVersion();
    }
}
