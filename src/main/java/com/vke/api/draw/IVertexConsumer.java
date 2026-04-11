package com.vke.api.draw;

import com.vke.api.rendering.abstraction.commands.CommandBuffer;
import com.vke.core.mesh.Mesh;
import com.vke.utils.io.Disposable;

public interface IVertexConsumer<T extends Vertex> extends Disposable  {
    void begin();

    void vertex(T... vertex);
    void index(int... index);

    void upload();

    void bindIBO(CommandBuffer cmd);
    void bindVBO(CommandBuffer cmd);
    void draw(CommandBuffer cmd);

    void mesh(Mesh<T> mesh);
}
