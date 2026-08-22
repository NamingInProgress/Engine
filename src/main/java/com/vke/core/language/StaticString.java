package com.vke.core.language;

import com.vke.core.Context;

public class StaticString implements Str {
    private final String content;

    StaticString(String content) {
        this.content = content;
    }

    @Override
    public String getContents(Context context) {
        return content;
    }
}
