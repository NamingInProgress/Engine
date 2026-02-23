package com.vke.api.pipeline;

public abstract class Entry {

    public String name;
    public int size;
    public long offset;
    public boolean auto;

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Entry e = (Entry) o;
        return name.equals(e.name) && size == e.size && offset == e.offset && auto == e.auto;
    }

}
