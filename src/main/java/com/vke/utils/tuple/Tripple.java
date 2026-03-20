package com.vke.utils.tuple;

import java.util.Objects;

public class Tripple<A, B, C> extends Pair<A, B> {

    public C v3;

    public Tripple(A v1, B v2, C v3) {
        super(v1, v2);
        this.v3 = v3;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Tripple<?, ?, ?> tripple = (Tripple<?, ?, ?>) o;
        return super.equals(new Pair<>(tripple.v1, tripple.v2)) && Objects.equals(v3, tripple.v3);
    }

    @Override
    public int hashCode() {
        return Objects.hash(v1, v2, v3);
    }

}
