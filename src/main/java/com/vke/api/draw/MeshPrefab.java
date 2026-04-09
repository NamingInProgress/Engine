package com.vke.api.draw;

public class MeshPrefab {
    private final float[][] positions;
    private final float[][] normals;
    private final int[] faceIDs;
    private final int[] indices;

    public MeshPrefab(float[][] positions, float[][] normals, int[] faceIDs, int[] indices) {
        this.positions = positions;
        this.normals = normals;
        this.faceIDs = faceIDs;
        this.indices = indices;
    }

    public Mesh toMesh(VertexFactory factory) {
        Vertex[] vertices = new Vertex[positions.length];
        for (int i = 0; i < positions.length; i++) {
            vertices[i] = factory.formatVertex(new PrefabVertex(positions[i], normals[i]), faceIDs[i]);
        }
        return new Mesh(vertices, indices);
    }

    public record PrefabVertex(float[] position, float[] normal) {
    }
}
