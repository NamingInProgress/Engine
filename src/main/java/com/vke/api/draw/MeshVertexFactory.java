package com.vke.api.draw;

import com.vke.core.mesh.MeshPrefab;

@FunctionalInterface
public interface MeshVertexFactory {
    Vertex formatVertex(MeshPrefab.PrefabVertex prefabVertex);
}
