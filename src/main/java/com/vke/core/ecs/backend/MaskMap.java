package com.vke.core.ecs.backend;

import com.vke.core.ecs.component.mask.ComponentMask;
import com.vke.core.ecs.component.mask.U64ComponentMask;

import java.util.function.Supplier;

public class MaskMap {
    private static final int BASE_SLOTS = 64;
    private static final float LOAD_FAC = 0.75f;

    private static final ComponentMask TOMBSTONE = new U64ComponentMask(0);

    private int SLOTS;
    private int MASK;

    private long[] hashes;
    private ComponentMask[] keys;
    private Archetype[] values;
    private int occupied;

    public MaskMap() {
        this.SLOTS = BASE_SLOTS;
        this.MASK = BASE_SLOTS - 1;

        this.hashes = new long[BASE_SLOTS];
        this.keys = new ComponentMask[BASE_SLOTS];
        this.values = new Archetype[BASE_SLOTS];
    }

    private static int hashToSlot(long hash) {
        hash ^= hash >>> 33;
        hash *= 0xff51afd7ed558ccdL;
        hash ^= hash >>> 33;
        return (int) hash;
    }

    public void insert(ComponentMask key, Archetype value) {
        long hash = hashToSlot(key.fastHash());
        insert0(hash, key, value);
    }

    private void rehash() {
        long[] oldHashes = hashes;
        ComponentMask[] oldKeys = keys;
        Archetype[] oldValues = values;

        SLOTS <<= 1;
        MASK = SLOTS - 1;

        hashes = new long[SLOTS];
        keys = new ComponentMask[SLOTS];
        values = new Archetype[SLOTS];
        occupied = 0;

        for (int i = 0; i < oldKeys.length; i++) {
            ComponentMask oldKey = oldKeys[i];
            if (oldKey != null && oldKey != TOMBSTONE) {
                insert02(oldHashes[i], oldKey, oldValues[i]);
            }
        }
    }

    private void insert0(long hash, ComponentMask key, Archetype value) {
        int slot = ((int) hash) & MASK;

        if (occupied == 0) {
            insert1(slot, hash, key, value);
            return;
        }

        while (true) {
            if (keys[slot] == null || keys[slot] == TOMBSTONE || (hashes[slot] == hash && keys[slot].fastEquals(key))) {
                insert1(slot, hash, key, value);
                break;
            }

            slot = (slot + 1) & MASK;
        }
    }

    private void insert1(int slot, long hash, ComponentMask key, Archetype value) {
        keys[slot] = key;
        hashes[slot] = hash;
        values[slot] = value;
        occupied++;

        float loadFac = ((float) occupied) / ((float) SLOTS);
        if (loadFac >= LOAD_FAC) {
            rehash();
        }
    }

    private void insert02(long hash, ComponentMask key, Archetype value) {
        int slot = ((int) hash) & MASK;

        if (occupied == 0) {
            insert2(slot, hash, key, value);
            return;
        }

        while (true) {
            if (keys[slot] == null || keys[slot] == TOMBSTONE || (hashes[slot] == hash && keys[slot].fastEquals(key))) {
                insert2(slot, hash, key, value);
                break;
            }

            slot = (slot + 1) & MASK;
        }
    }

    private void insert2(int slot, long hash, ComponentMask key, Archetype value) {
        keys[slot] = key;
        hashes[slot] = hash;
        values[slot] = value;
        occupied++;
    }

    public Archetype find(ComponentMask key) {
        //fast path: if nothing in here, nothing to look for!
        if (occupied == 0) return null;

        long hash = hashToSlot(key.fastHash());
        int slot = ((int) hash) & MASK;

        ComponentMask marker = keys[slot];
        //there wasnt any value
        if (marker == null) return null;
        //we got lucky first try, this is a super fast path in the early stages of ecs when everything is still empty!
        if (hash == hashes[slot] && marker.fastEquals(key)) {
            return values[slot];
        }

        slot = (slot + 1) & MASK;

        int tries = 0;
        while (true) {
            marker = keys[slot];
            //when the linear probe finds null, were done
            if (marker == null){
                return null;
            }
            if (marker == TOMBSTONE) {
                slot = (slot + 1) & MASK;
                continue;
            }
            if (hash == hashes[slot] && marker.fastEquals(key)) {
                return values[slot];
            }

            slot = (slot + 1) & MASK;
            tries++;
        }
    }

    public Archetype findOrMake(ComponentMask key, Supplier<Archetype> creator) {
        //fast path: if nothing in here, nothing to look for!
        long hash = hashToSlot(key.fastHash());
        int slot = ((int) hash) & MASK;

        ComponentMask marker = keys[slot];
        //there wasnt any value
        if (marker == null) {
            Archetype value = creator.get();
            insert1(slot, hash, key, value);
            return value;
        }
        //we got lucky first try, this is a super fast path in the early stages of ecs when everything is still empty!
        if (hash == hashes[slot] && marker.fastEquals(key)) return values[slot];

        slot = (slot + 1) & MASK;

        while (true) {
            marker = keys[slot];
            //when the linear probe finds null, were done
            if (marker == null) {
                Archetype value = creator.get();
                insert1(slot, hash, key, value);
                return value;
            }
            if (marker == TOMBSTONE) {
                slot++;
                continue;
            }
            if (hash == hashes[slot] && marker.fastEquals(key)) return values[slot];

            slot = (slot + 1) & MASK;
        }
    }

    public void remove(ComponentMask key) {
        if (occupied == 0) return;

        long hash = hashToSlot(key.fastHash());
        int slot = ((int) hash) & MASK;

        ComponentMask marker = keys[slot];
        //there wasnt any value
        if (marker == null) return;
        //we got lucky first try, this is a super fast path in the early stages of ecs when everything is still empty!
        if (hash == hashes[slot] && marker.fastEquals(key)) {
            keys[slot] = TOMBSTONE;
            occupied--;
        }

        slot = (slot + 1) & MASK;

        while (true) {
            marker = keys[slot];
            //when the linear probe finds null, were done
            if (marker == null) return;
            if (marker == TOMBSTONE) {
                slot++;
                continue;
            }
            if (hash == hashes[slot] && marker.fastEquals(key)) {
                keys[slot] = TOMBSTONE;
                occupied--;
                return;
            }

            slot = (slot + 1) & MASK;
        }
    }
}
