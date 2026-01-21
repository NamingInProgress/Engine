package com.vke.core.parsing.source;

import com.vke.api.parsing.SourceCode;

public class StringSourceCode implements SourceCode {
    private String s;
    private int index;

    public StringSourceCode(String s) {
        this.s = s;
    }

    @Override
    public char next() {
        return s.charAt(index++);
    }

    @Override
    public boolean hasNext() {
        return index < s.length();
    }
}
