package com.vke.utils.collection;

import java.util.concurrent.CopyOnWriteArrayList;

public abstract class AbstractGlossary<SELF> {
    protected final CopyOnWriteArrayList<SELF> entries = new CopyOnWriteArrayList<>();

    public void addEntry(SELF entry) {
        entries.add(entry);
    }

    public void removeEntry(SELF entry) {
        entries.remove(entry);
    }
}
