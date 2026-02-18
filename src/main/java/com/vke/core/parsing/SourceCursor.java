package com.vke.core.parsing;

public class SourceCursor {
    private final char[] source;
    private int index;
    private int marker;

    public SourceCursor(char[] source, int index) {
        this.source = source;
        this.index = index;
    }

    public char nextChar() throws EOF {
        if (index >= source.length) throw new EOF();
        return source[index++];
    }

    public char peekChar() {
        return peekChar(0);
    }

    public char peekChar(int dist) {
        if (index + dist >= source.length) return '\0';
        return source[index + dist];
    }

    public void skipWhitespaces() throws EOF {
        while (true) {
            char p = peekChar();
            if (Character.isWhitespace(p)) {
                nextChar();
            } else {
                break;
            }
        }
    }

    public void mark() {
        marker = index;
    }

    public void reset() {
        index = marker;
    }

    public static class EOF extends Exception {}
}
