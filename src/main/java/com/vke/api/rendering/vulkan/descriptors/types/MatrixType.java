package com.vke.api.rendering.vulkan.descriptors.types;

import java.util.Objects;

public class MatrixType extends TypeLayout {

    public int rows;
    public int columns;
    public long stride;

    public MatrixType(int rows, int columns, long stride) {
        this.rows = rows;
        this.columns = columns;
        this.stride = stride;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        MatrixType that = (MatrixType) o;
        return rows == that.rows && columns == that.columns && stride == that.stride;
    }

    @Override
    public int hashCode() {
        return Objects.hash(rows, columns, stride);
    }
}
