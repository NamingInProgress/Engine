package com.vke.core.rendering.graph;

import com.vke.core.Context;
import com.vke.core.ContextWrapper;

import java.util.HashMap;
import java.util.Map;

public class GraphContext extends ContextWrapper {
    private final Map<String, Object> fields;

    public GraphContext(Context baseContext) {
        super(baseContext);
        this.fields = new HashMap<>();
    }

    public void clear() {
        this.fields.clear();
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String keyOfT) {
        return (T) fields.get(keyOfT);
    }

    public void put(String key, Object object) {
        this.fields.put(key, object);
    }
}
