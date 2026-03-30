package com.vke.utils.io;

import java.util.Objects;

public class SegmentedPath {
    private final String[] parts;
    private final String toString;
    private final int hash;

    public SegmentedPath(String[] parts) {
        this(parts, ".");
    }

    public SegmentedPath(String[] parts, String delimiter) {
        this.parts = parts;
        this.toString = String.join(delimiter, parts);
        this.hash = toString.hashCode();
    }

    public SegmentedPath(String literal) {
        this(literal.isEmpty() ? new String[0] : literal.split("\\."), ".");
    }

    public SegmentedPath(String literal, String delimiter) {
        this(literal.isEmpty() ? new String[0] : literal.split(delimiter), delimiter);
    }

    @Override
    public int hashCode() {
        return hash;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        SegmentedPath segmentedPath = (SegmentedPath) o;
        return Objects.deepEquals(parts, segmentedPath.parts);
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
