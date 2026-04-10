package com.vke.api.draw;

public class MeshPrefab {
    private final PrefabVertex[] vertices;
    private final int[] indices;

    public MeshPrefab(PrefabVertex[] vertices, int[] indices) {
        this.vertices = vertices;
        this.indices = indices;
    }

    public Mesh toMesh(VertexFactory factory) {
        Vertex[] vertices = new Vertex[this.vertices.length];
        for (int i = 0; i < vertices.length; i++) {
            vertices[i] = factory.formatVertex(this.vertices[i]);
        }
        return new Mesh(vertices, indices);
    }

    public record PrefabVertex(float[] position, float[] normal, float[] uv) {
    }
}
