package com.vke.api.draw;

import com.vke.core.mesh.MeshPrefab;

@FunctionalInterface
public interface VertexFactory {
    Vertex formatVertex(MeshPrefab.PrefabVertex prefabVertex);
}
