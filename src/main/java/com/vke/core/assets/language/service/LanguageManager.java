package com.vke.core.assets.language.service;

import com.vke.api.services2.Service;
import com.vke.core.assets.language.Language;
import com.vke.utils.io.Identifier;

import java.io.IOException;

public interface LanguageManager extends Service {
    void changeLanguage(String language) throws IOException;
    void changeLanguage(Identifier newLanguage) throws IOException;
    Language getCurrentLanguage();
    long getVersion();
}
