package com.vke.api.draw;

import com.vke.api.rendering.abstraction.commands.CommandBuffer;
import com.vke.core.mesh.Mesh;
import com.vke.core.rendering.draw.DrawContext;
import com.vke.utils.io.Disposable;

public interface IVertexConsumer<T extends Vertex> extends Disposable, IDrawable  {
    void begin();

    void vertex(T... vertex);
    void index(int... index);

    void upload();

    void mesh(Mesh<T> mesh);
}
