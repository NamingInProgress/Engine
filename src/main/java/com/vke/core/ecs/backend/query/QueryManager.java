package com.vke.core.ecs.backend.query;

import com.carrotsearch.hppc.IntObjectHashMap;
import com.carrotsearch.hppc.ObjectArrayList;
import com.carrotsearch.hppc.cursors.IntObjectCursor;
import com.carrotsearch.hppc.cursors.ObjectCursor;
import com.vke.core.ecs.api.Query;
import com.vke.core.ecs.backend.Archetype;
import com.vke.core.ecs.component.mask.ComponentMask;

public class QueryManager {
    private final IntObjectHashMap<ObjectArrayList<QueryCache>> categories;

    public QueryManager() {
        this.categories = new IntObjectHashMap<>();
    }

    public void registerQuery(int category, Query query) {
        var l = categories.get(category);
        if (l == null) {
            l = new ObjectArrayList<>();
            categories.put(category, l);
        }
        l.add(new QueryCache(query));
    }

    public long runCategory(int cat) {
        var list = categories.get(cat);
        long count = 0;
        if (list != null) {
            int limit = list.elementsCount;
            for (int i = 0; i < limit; i++) {
                count += ((QueryCache) list.buffer[i]).execute();
            }
        }
        return count;
    }

    public void onEntityBatchSpawn(ComponentMask mask, Archetype at, int i, int n) {
        for (IntObjectCursor<ObjectArrayList<QueryCache>> c0 : categories) {
            for (ObjectCursor<QueryCache> c1 : c0.value) {
                c1.value.onEntityBatchSpawn(mask, at, i, n);
            }
        }
    }

    public void onEntityTransitionOut(Archetype at) {
        for (IntObjectCursor<ObjectArrayList<QueryCache>> c0 : categories) {
            for (ObjectCursor<QueryCache> c1 : c0.value) {
                c1.value.onEntityTransitionOut(at);
            }
        }
    }

    public void onEntityTransitionIn(ComponentMask mask, Archetype at, int i) {
        for (IntObjectCursor<ObjectArrayList<QueryCache>> c0 : categories) {
            for (ObjectCursor<QueryCache> c1 : c0.value) {
                c1.value.onEntityTransitionIn(mask, at, i);
            }
        }
    }

    public void onEntityDestroyed(Archetype at) {
        for (IntObjectCursor<ObjectArrayList<QueryCache>> c0 : categories) {
            for (ObjectCursor<QueryCache> c1 : c0.value) {
                c1.value.onEntityDestroyed(at);
            }
        }
    }

    public void onConsecutiveEntitiesDestroyed(Archetype at, int length) {
        for (IntObjectCursor<ObjectArrayList<QueryCache>> c0 : categories) {
            for (ObjectCursor<QueryCache> c1 : c0.value) {
                c1.value.onConsecutiveEntitiesDestroyed(at, length);
            }
        }
    }
}
