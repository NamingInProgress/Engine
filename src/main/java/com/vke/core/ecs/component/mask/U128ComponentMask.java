package com.vke.core.ecs.component.mask;

final class U128ComponentMask implements ComponentMask {
    final long lower;
    final long upper;

    public U128ComponentMask(long lower, long upper) {
        this.lower = lower;
        this.upper = upper;
    }

    @Override
    public boolean contains(ComponentMask other) {
        return switch (other) {
            case U64ComponentMask m -> (lower & m.mask) == m.mask && upper == 0;
            case U128ComponentMask m -> (lower & m.lower) == m.lower && (upper & m.upper) == m.upper;
            case WideComponentMask m -> m.contains(this);
        };
    }

    @Override
    public ComponentMask extend(ComponentMask other) {
        return switch (other) {
            case U64ComponentMask m -> new U128ComponentMask(lower | m.mask, upper);
            case U128ComponentMask m -> new U128ComponentMask(lower | m.lower, upper | m.upper);
            case WideComponentMask m -> m.extend(this);
        };
    }

    @Override
    public long fastHash() {
        return lower ^ Long.rotateLeft(upper, 13);
    }

    @Override
    public boolean fastEquals(ComponentMask other) {
        return switch (other) {
            case U64ComponentMask m -> upper == 0 && lower == m.mask;
            case U128ComponentMask m -> lower == m.lower && upper == m.upper;
            case WideComponentMask m -> m.things.length == 2 && lower == m.things[0] && upper == m.things[1];
        };
    }
}
