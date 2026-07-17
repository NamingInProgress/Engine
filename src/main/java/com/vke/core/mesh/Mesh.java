package com.vke.core.mesh;

import com.vke.api.rendering.abstraction.draw.Vertex;

public class Mesh<T extends Vertex> {
    private final T[] vertices;
    private final int[] indices;
    private final int maxIndex;

    public Mesh(T[] vertices, int[] indices) {
        this.vertices = vertices;
        this.indices = indices;
        int maxIndex = 0;
        for (int index : indices) {
            maxIndex = Math.max(index, maxIndex);
        }
        this.maxIndex = maxIndex;
    }

    public T[] getVertices() {
        return vertices;
    }

    public int[] getIndices() {
        return indices;
    }

    public int getMaxIndex() {
        return maxIndex;
    }
}
