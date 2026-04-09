package com.vke.api.draw;

public class Mesh {
    private final Vertex[] vertices;
    private final int[] indices;

    public Mesh(Vertex[] vertices, int[] indices) {
        this.vertices = vertices;
        this.indices = indices;
    }

    public Vertex[] getVertices() {
        return vertices;
    }

    public int[] getIndices() {
        return indices;
    }
}
