package com.vke.api.draw;

import com.vke.core.mesh.Mesh;

public interface IVertexConsumer<T extends Vertex> {
    void begin();
    void vertex(T vertex);
    void index(int index);
    void upload();

    void mesh(Mesh<T> mesh);
}
