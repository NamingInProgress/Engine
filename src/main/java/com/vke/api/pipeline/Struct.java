package com.vke.api.pipeline;

import java.util.LinkedHashMap;
import java.util.Objects;

public abstract class Struct {

    protected final LinkedHashMap<String, Entry> entries = new LinkedHashMap<>();

    public int sizeof() { return entries.values().stream().mapToInt(e -> e.size).sum(); }

    public Entry getEntry(String name) { return entries.get(name); }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Struct struct = (Struct) o;
        return Objects.equals(entries, struct.entries);
    }

}
