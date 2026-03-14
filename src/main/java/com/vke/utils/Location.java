package com.vke.utils;

import java.util.Objects;

public class Location {
    private final String[] parts;
    private final String toString;
    private final int hash;

    public Location(String[] parts) {
        this.parts = parts;
        this.toString = String.join(".", parts);
        this.hash = toString.hashCode();
    }

    public Location(String literal) {
        this(literal.split("\\."));
    }

    @Override
    public int hashCode() {
        return hash;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Location location = (Location) o;
        return Objects.deepEquals(parts, location.parts);
    }

    @Override
    public String toString() {
        return toString;
    }

    public String[] getParts() {
        return parts;
    }

    public String last() {
        if (parts.length > 0) {
            return parts[parts.length - 1];
        }
        return null;
    }
}
