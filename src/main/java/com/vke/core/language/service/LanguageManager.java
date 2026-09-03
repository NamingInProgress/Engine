package com.vke.core.language.service;

import com.vke.api.services2.Service;
import com.vke.core.Identifier;
import com.vke.core.language.Language;

import java.io.IOException;

public interface LanguageManager extends Service {
    void changeLanguage(String language) throws IOException;
    void changeLanguage(Identifier newLanguage) throws IOException;
    Language getCurrentLanguage();
    long getVersion();
}
