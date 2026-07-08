package com.vke.api.draw;

import com.vke.api.rendering.abstraction.data.Texture;
import com.vke.core.mesh.Mesh;
import com.vke.core.rendering.transform.MatrixStack;
import com.vke.utils.io.Disposable;

public interface VertexConsumer<T extends Vertex> extends Disposable, Drawable {
    void beginFrame();
    void begin();

    void vertices(T... vertices);
    default void vertices(Texture usesTexture, T... vertices) { vertices(vertices); }
    void indices(int... indices);

    void upload();

    void mesh(Mesh<T> mesh);

    MatrixStack getMatrixStack();
}
