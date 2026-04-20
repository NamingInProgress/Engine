package com.vke.core.assets.language;

import com.vke.core.Context;
import com.vke.core.assets.language.service.LanguageManagerScopedImpl;
import com.vke.core.services2.Services;
import com.vke.utils.io.SegmentedPath;

public class MultilingualString implements Str {
    private final SegmentedPath key;
    private LanguageManagerScopedImpl languageManager;
    private long targetVersion;
    private String cache;

    MultilingualString(SegmentedPath key) {
        this.key = key;
        this.targetVersion = Long.MIN_VALUE;
    }

    @Override
    public String getContents(Context context) {
        if (languageManager == null) {
            languageManager = context.service(Services.LANGUAGE_MANAGER);
        }

        if (targetVersion != languageManager.getVersion()) {
            Language language = languageManager.getCurrentLanguage();
            this.cache = language.find(key);

            targetVersion = languageManager.getVersion();
        }

        return cache;
    }
}
