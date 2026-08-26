package com.vke.core.ecs.component.mask;

import com.vke.utils.exception.Unreachable;

import java.util.Arrays;

public class ComponentMask {
    private final int[] ids;
    private final long hash;
    private final int first;
    private final int last;

    public ComponentMask(int... ids) {
        this(false, ids);
    }

    public ComponentMask(boolean sorted, int... ids) {
        if (!sorted) {
            Arrays.sort(ids);
        }

        this.ids = ids;
        this.first = ids[0];
        this.last = ids[ids.length - 1];

        long h = 0x9E3779B97F4A7C15L;

        for (int id : ids) {
            h ^= id * 0x9E3779B97F4A7C15L;
            h = Long.rotateLeft(h, 27);
            h *= 0xC2B2AE3D27D4EB4FL;
        }

        this.hash = h;
    }

    public boolean contains(ComponentMask other) {
        if (other.ids.length == 0) return true;
        if (this.ids.length < other.ids.length) return false;

        if (other.first < this.first || other.last > this.last) {
            return false;
        }

        int a = 0;
        int b = 0;

        while (a < ids.length && b < other.ids.length) {
            if (ids[a] < other.ids[b]) {
                a++;
            } else if (ids[a] == other.ids[b]) {
                a++;
                b++;
            } else {
                return false;
            }
        }

        return b == other.ids.length;
    }

    public ComponentMask union(ComponentMask other) {
        throw new Unreachable();
    }

    public ComponentMask addComponent(int id) {
        int idx = Arrays.binarySearch(ids, id);
        if (idx >= 0) return this;
        int whereToInsert = ~idx;
        int l = ids.length - whereToInsert;
        int[] newIds = Arrays.copyOf(ids, ids.length + 1);
        System.arraycopy(newIds, whereToInsert, newIds, whereToInsert + 1, l);
        newIds[whereToInsert] = id;
        return new ComponentMask(true, newIds);
    }

    public ComponentMask addComponents(int... ids) {
        if (ids.length == 0) return this;
        if (ids.length == 1) return addComponent(ids[0]);

        int[] additions = ids.clone();
        Arrays.sort(additions);

        int unique = 1;
        for (int i = 1; i < additions.length; i++) {
            if (additions[i] != additions[unique - 1]) {
                additions[unique++] = additions[i];
            }
        }

        int[] result = new int[this.ids.length + unique];

        int a = 0;
        int b = 0;
        int r = 0;

        while (a < this.ids.length && b < unique) {
            int x = this.ids[a];
            int y = additions[b];

            if (x < y) {
                result[r++] = x;
                a++;
            } else if (x > y) {
                result[r++] = y;
                b++;
            } else {
                result[r++] = x;
                a++;
                b++;
            }
        }

        while (a < this.ids.length) result[r++] = this.ids[a++];
        while (b < unique) result[r++] = additions[b++];

        if (r == this.ids.length) return this;

        return new ComponentMask(true, Arrays.copyOf(result, r));
    }

    public ComponentMask removeComponent(int id) {
        int idx = Arrays.binarySearch(ids, id);
        if (idx < 0) return this;
        int[] newIds = ids.clone();
        System.arraycopy(newIds, idx + 1, newIds, idx, ids.length - idx - 1);
        newIds = Arrays.copyOf(newIds, ids.length - 1);
        return new ComponentMask(true, newIds);
    }

    public ComponentMask removeComponents(int... ids) {
        if (ids.length == 0) return this;
        if (ids.length == 1) return removeComponent(ids[0]);

        int[] removals = ids.clone();
        Arrays.sort(removals);

        int unique = 1;
        for (int i = 1; i < removals.length; i++) {
            if (removals[i] != removals[unique - 1]) {
                removals[unique++] = removals[i];
            }
        }

        int[] result = new int[this.ids.length];

        int a = 0;
        int b = 0;
        int r = 0;

        while (a < this.ids.length && b < unique) {
            int x = this.ids[a];
            int y = removals[b];

            if (x < y) {
                result[r++] = x;
                a++;
            } else if (x > y) {
                b++;
            } else {
                a++;
                b++;
            }
        }

        while (a < this.ids.length) result[r++] = this.ids[a++];

        if (r == this.ids.length) return this;

        return new ComponentMask(true, Arrays.copyOf(result, r));
    }

    public long fastHash() {
        return hash;
    }

    public boolean fastEquals(ComponentMask other) {
        if (ids.length != other.ids.length) return false;
        if (hash != other.hash) return false;
        if (first != other.first) return false;
        if (last != other.last) return false;

        return Arrays.equals(ids, other.ids);
    }

    public int componentCount() {
        return ids.length;
    }

    public int[] getComponents() {
        return ids;
    }
}
