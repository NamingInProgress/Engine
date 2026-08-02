package com.vke.api.rendering.abstraction.draw;

import com.vke.core.mesh.Mesh;
import com.vke.utils.io.Disposable;

public interface VertexConsumer<T extends Vertex> extends Disposable, Drawable {
    void beginFrame();
    void begin();

    void vertices(T... vertices);
    void indices(int... indices);

    void upload();

    void mesh(Mesh<T> mesh);
}
