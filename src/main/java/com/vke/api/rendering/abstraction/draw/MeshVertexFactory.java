package com.vke.api.rendering.abstraction.draw;

import com.vke.core.mesh.MeshPrefab;

@FunctionalInterface
public interface MeshVertexFactory {
    Vertex formatVertex(MeshPrefab.PrefabVertex prefabVertex);
}
