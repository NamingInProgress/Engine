package com.vke.api.language;

import com.vke.utils.Identifier;
import com.vke.utils.Location;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Locale;

public class Language {
    private final Locale locale;
    private final HashMap<Location, String> items;

    public Language(Locale locale) {
        this.locale = locale;
        this.items = new HashMap<>();
    }

    public String find(Location identifier) {
        return items.get(identifier);
    }

    public void setItem(Location key, String value) {
        items.put(key, value);
    }

    public Locale getLocale() {
        return locale;
    }
}
