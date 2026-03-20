package com.vke.core.assets.language;

import com.vke.core.VKEngine;

public class StaticString implements Str {
    private final String content;

    StaticString(String content) {
        this.content = content;
    }

    @Override
    public String getContents(VKEngine engine) {
        return content;
    }
}
