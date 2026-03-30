package com.vke.core.assets.language;

import com.vke.utils.io.SegmentedPath;

import java.util.HashMap;
import java.util.Locale;

public class Language {
    private final Locale locale;
    private final HashMap<SegmentedPath, String> items;

    public Language(Locale locale) {
        this.locale = locale;
        this.items = new HashMap<>();
    }

    public String find(SegmentedPath identifier) {
        return items.get(identifier);
    }

    public void setItem(SegmentedPath key, String value) {
        items.put(key, value);
    }

    public Locale getLocale() {
        return locale;
    }
}
