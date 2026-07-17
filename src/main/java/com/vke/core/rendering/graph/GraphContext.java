package com.vke.core.rendering.graph;

import com.vke.core.Context;
import com.vke.core.ContextWrapper;
import com.vke.utils.io.Identifier;
import com.vke.utils.iter.helpers.Option;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GraphContext extends ContextWrapper {
    private final Map<String, Object> fields;
    private List<Identifier> postDisabledStages;

    public GraphContext(Context baseContext) {
        super(baseContext);
        this.fields = new HashMap<>();
        this.postDisabledStages = null;
    }

    public void clear() {
        this.fields.clear();

        this.postDisabledStages = null;
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String keyOfT) {
        return (T) fields.get(keyOfT);
    }

    @SuppressWarnings("unchecked")
    public <T> Option<T> getOrDefault(String keyOfT) {
        return (Option<T>) Option.useIfNotNull(fields.get(keyOfT));
    }

    public void put(String key, Object object) {
        this.fields.put(key, object);
    }

    //ENGINE INTERNAL SHIT
    public void setPostDisabledStages(List<Identifier> list) {
        this.postDisabledStages = list;
    }
    public List<Identifier> getPostDisabledStages() {
        return postDisabledStages;
    }
}
