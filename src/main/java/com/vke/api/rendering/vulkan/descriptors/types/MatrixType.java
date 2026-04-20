package com.vke.api.rendering.vulkan.descriptors.types;

public class MatrixType extends TypeLayout {

    public int rows;
    public int columns;
    public long stride;

    public MatrixType(int rows, int columns, long stride) {
        this.rows = rows;
        this.columns = columns;
        this.stride = stride;
    }
}
