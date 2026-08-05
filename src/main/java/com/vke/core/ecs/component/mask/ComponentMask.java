package com.vke.core.ecs.component.mask;

import com.vke.utils.exception.Unreachable;

import java.util.Arrays;

public class ComponentMask {
    private final int[] ids;
    private final long hash;
    private final int first;
    private final int last;

    public ComponentMask(int... ids) {
        Arrays.sort(ids);

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
