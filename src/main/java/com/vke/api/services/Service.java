package com.vke.api.services;

import java.util.List;
import com.vke.utils.Disposable;

public abstract class Service implements Disposable {
    protected String id;
    private List<String> depCache;

    public Service(String id) {
        this.id = id;
    }

    public String getId() { return this.id; }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof Service s)) return false;
        return s.id.equals(id);
    }

    protected abstract List<String> dependencies();

    public List<String> getDependencies() {
        if (depCache == null) depCache = dependencies();
        return depCache;
    }
}
