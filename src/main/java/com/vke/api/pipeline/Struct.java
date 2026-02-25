package com.vke.api.pipeline;

import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Objects;

public class Struct {

    public final HashMap<String, Entry> entries = new HashMap<>();

    public int sizeof() { return entries.values().stream().mapToInt(e -> (int) e.size).sum(); }

    public Entry getEntry(String name) { return entries.get(name); }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Struct struct = (Struct) o;
        return Objects.equals(entries, struct.entries);
    }

}
