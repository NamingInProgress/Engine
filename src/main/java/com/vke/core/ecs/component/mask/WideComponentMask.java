package com.vke.core.ecs.component.mask;

import jdk.incubator.vector.*;

import java.util.Arrays;

public final class WideComponentMask implements ComponentMask {
    long[] things;

    private static final VectorSpecies<Long> SPECIES = LongVector.SPECIES_MAX;

    public WideComponentMask(long... mask) {
        this.things = mask.clone();
    }

    public WideComponentMask(int bit) {
        this.things = new long[Math.ceilDiv(bit + 1, 64)];
        setBit(bit);
    }

    @Override
    public boolean contains(ComponentMask other) {
        return switch (other) {
            case U64ComponentMask m -> things.length > 0 && (things[0] & m.mask) == m.mask;
            case U128ComponentMask m -> things.length > 1 &&
                            (things[0] & m.lower) == m.lower &&
                            (things[1] & m.upper) == m.upper;
            case WideComponentMask m -> {
                if (things.length < m.things.length) yield false;
                int limit = m.things.length;
                for (int i = 0; i < limit; i++) {
                    long a = things[i];
                    long b = m.things[i];
                    if ((a & b) != b) yield false;
                }
                yield true;
            }
        };
    }

    private void realloc(int thingyCount) {
        this.things = Arrays.copyOf(things, thingyCount);
    }

    public void setBit(int bit) {
        int thingIndex = bit >>> 6;
        if (thingIndex >= things.length) {
            realloc(thingIndex + 1);
        }
        things[thingIndex] |= 1L << (bit & (64 - 1));
    }

    @Override
    public ComponentMask extend(ComponentMask other) {
        return switch (other) {
            case U64ComponentMask m -> {
                long[] a = Arrays.copyOf(things, Math.max(1, things.length));
                a[0] |= m.mask;
                yield new WideComponentMask(a);
            }
            case U128ComponentMask m -> {
                long[] a = Arrays.copyOf(things, Math.max(2, things.length));
                a[0] |= m.lower;
                a[1] |= m.upper;
                yield new WideComponentMask(a);
            }
            case WideComponentMask m -> {
                long[] a = Arrays.copyOf(things, Math.max(things.length, m.things.length));
                for (int i = 0; i < m.things.length; i++)
                    a[i] |= m.things[i];
                yield new WideComponentMask(a);
            }
        };
    }

    @Override
    public long fastHash() {
        long h = 0;

        for (long x : things) {
            h = Long.rotateLeft(h, 13) ^ x;
        }

        return h;
    }

    @Override
    public boolean fastEquals(ComponentMask other) {
        return switch (other) {
            case U64ComponentMask m -> things.length == 1 && things[0] == m.mask;
            case U128ComponentMask m -> things.length == 2 && things[0] == m.lower && things[1] == m.upper;
            case WideComponentMask m -> {
                if (things.length != m.things.length) {
                    yield false;
                }

                int i = 0;
                int upper = SPECIES.loopBound(things.length);

                for (; i < upper; i += SPECIES.length()) {
                    LongVector a = LongVector.fromArray(SPECIES, things, i);
                    LongVector b = LongVector.fromArray(SPECIES, m.things, i);

                    if (!a.compare(VectorOperators.EQ, b).allTrue()) {
                        yield false;
                    }
                }

                for (; i < things.length; i++) {
                    if (things[i] != m.things[i]) {
                        yield false;
                    }
                }

                yield true;
            }
        };
    }
}
