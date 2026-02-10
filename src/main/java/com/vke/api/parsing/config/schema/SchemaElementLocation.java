package com.vke.api.parsing.config.schema;

import java.util.ArrayDeque;

public class SchemaElementLocation {
    private final String file;
    private final ArrayDeque<String> path;

    public SchemaElementLocation(String file) {
        this.file = file;
        this.path = new ArrayDeque<>();
    }

    public void push(String segment) {
        this.path.addLast(segment);
    }

    public void pop() {
        this.path.removeLast();
    }

    public String getFormatted() {
        String p = String.join(".", path);
        return String.format("\tat: %s\n\tat: %s", p, file);
    }
}
