package com.vke.api.pipeline;

import com.carrotsearch.hppc.ObjectLongHashMap;

import java.util.ArrayList;

public abstract class Struct {

    protected final ArrayList<Entry> entries = new ArrayList<>();
    protected final ObjectLongHashMap<Entry> precedings = new ObjectLongHashMap<>();

    public ArrayList<Entry> getEntries() {
        return this.entries;
    }

    public Entry byName(String name) {
        return entries.stream().filter(c -> c.name.equals(name)).findFirst().orElse(null);
    }

    public long preceding(String name) {
        return preceding(byName(name));
    }

    public long preceding(Entry e) {
        if (precedings.containsKey(e)) return precedings.get(e);

        int idx = entries.indexOf(e);

        long count = 0;
        for (int i = 0; i < idx; i++) {
            count += entries.get(i).getSize();
        }

        precedings.put(e, count);

        return count;
    }

    public int sizeof() {
        return entries.stream().mapToInt(Entry::getSize).sum();
    }

}
