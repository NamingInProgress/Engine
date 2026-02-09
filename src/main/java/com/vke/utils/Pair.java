package com.vke.utils;

import java.util.Objects;

public class Pair<T, U> {

    public T v1;
    public U v2;

    public Pair(T v1, U v2) {
        this.v1 = v1;
        this.v2 = v2;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Pair<?, ?> pair = (Pair<?, ?>) o;
        return Objects.equals(v1, pair.v1) && Objects.equals(v2, pair.v2);
    }

    @Override
    public int hashCode() {
        return Objects.hash(v1, v2);
    }
}
