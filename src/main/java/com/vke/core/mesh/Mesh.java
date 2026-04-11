package com.vke.core.mesh;

import com.vke.api.draw.Vertex;

public class Mesh<T extends Vertex> {
    private final T[] vertices;
    private final int[] indices;

    public Mesh(T[] vertices, int[] indices) {
        this.vertices = vertices;
        this.indices = indices;
    }

    public T[] getVertices() {
        return vertices;
    }

    public int[] getIndices() {
        return indices;
    }
}
