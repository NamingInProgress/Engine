package com.vke.core.ecs.component.mask;

public final class U64ComponentMask implements ComponentMask {
    final long mask;

    public U64ComponentMask(int bit) {
        this.mask = 1L << bit;
    }

    public U64ComponentMask(long mask) {
        this.mask = mask;
    }

    @Override
    public boolean contains(ComponentMask other) {
        return switch (other) {
            case U64ComponentMask m -> (mask & m.mask) == m.mask;
            case U128ComponentMask m -> m.upper == 0 && (mask & m.lower) == m.lower;
            case WideComponentMask m -> m.contains(this);
        };
    }

    @Override
    public ComponentMask extend(ComponentMask other) {
        return switch (other) {
            case U64ComponentMask m -> new U64ComponentMask(mask | m.mask);
            case U128ComponentMask m -> new U128ComponentMask(mask | m.lower, m.upper);
            case WideComponentMask m -> m.extend(this);
        };
    }

    @Override
    public long fastHash() {
        return mask;
    }

    @Override
    public boolean fastEquals(ComponentMask other) {
        return switch (other) {
            case U64ComponentMask m -> mask == m.mask;
            case U128ComponentMask m -> m.upper == 0 && mask == m.lower;
            case WideComponentMask m -> m.things.length == 1 && mask == m.things[0];
        };
    }
}
