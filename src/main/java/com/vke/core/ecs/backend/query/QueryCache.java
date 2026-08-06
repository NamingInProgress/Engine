package com.vke.core.ecs.backend.query;

import com.vke.core.VKEngine;
import com.vke.core.ecs.api.Query;
import com.vke.core.ecs.backend.Archetype;
import com.vke.core.ecs.component.mask.ComponentMask;

import java.util.ArrayList;

public class QueryCache {
    private final Query query;
    private final ComponentMask mask;
    private final ArrayList<Entry> entries;

    public QueryCache(Query query) {
        this.query = query;
        this.mask = query.getMask();
        this.entries = new ArrayList<>();
    }

    public long execute() {
        long c = 0;
        for (Entry entry : entries) {
            c += entry.i1 - entry.i0;
            query.execute(entry.archetype, entry.i0, entry.i1);
        }
        return c;
    }

    public void onEntityBatchSpawn(ComponentMask mask, Archetype at, int i, int n) {
        if (mask.contains(this.mask)) {
            for (Entry entry : entries) {
                if (entry.archetype.getId() == at.getId()) {
                    entry.i1 = i + n;
                    return;
                }
            }

            //nothing found dang make new now or go insane like i am right now
            Entry newEntry = new Entry(at, i, i + n);
            entries.add(newEntry);
        }
    }

    public void onEntityTransitionOut(Archetype at) {
        onEntityDestroyed(at);
    }

    public void onEntityTransitionIn(ComponentMask mask, Archetype at, int i) {
        onEntityBatchSpawn(mask, at, i, 1);
    }

    public void onEntityDestroyed(Archetype at) {
        for (var iter = entries.listIterator(); iter.hasNext(); ) {
            var entry = iter.next();
            if (entry.archetype.getId() == at.getId()) {
                entry.i1--;

                if (entry.i0 >= entry.i1) {
                    iter.remove();
                }
                return;
            }
        }
    }

    public void onConsecutiveEntitiesDestroyed(Archetype at, int length) {
        for (var iter = entries.listIterator(); iter.hasNext(); ) {
            var entry = iter.next();
            if (entry.archetype.getId() == at.getId()) {
                entry.i1 -= length;

                if (entry.i0 > entry.i1) {
                    iter.remove();
                }
                return;
            }
        }
    }

    private static final class Entry {
        private final Archetype archetype;
        private int i0;
        private int i1;

        private Entry(Archetype archetype, int i0, int i1) {
            this.archetype = archetype;
            this.i0 = i0;
            this.i1 = i1;
        }
    }
}
