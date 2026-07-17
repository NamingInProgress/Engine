package com.vke.core.ecs;

public class U64ComponentMask implements ComponentMask {
    private final long mask;

    public U64ComponentMask(int bit) {
        this.mask = 1L << bit;
    }

    public U64ComponentMask(long mask) {
        this.mask = mask;
    }

    @Override
    public boolean contains(ComponentMask other) {
        if (other instanceof U64ComponentMask m) {
            return (mask & m.mask) != 0;
        }
        return false;
    }

    @Override
    public ComponentMask extend(ComponentMask mask) {
        if (mask instanceof U64ComponentMask m) {
            return new U64ComponentMask(this.mask | m.mask);
        }
        return this;
    }
}
