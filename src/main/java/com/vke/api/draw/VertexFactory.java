package com.vke.api.draw;

@FunctionalInterface
public interface VertexFactory {
    Vertex formatVertex(MeshPrefab.PrefabVertex prefabVertex);
}
