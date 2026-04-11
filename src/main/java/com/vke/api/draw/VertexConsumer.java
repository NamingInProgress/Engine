package com.vke.api.draw;

import com.vke.core.vulkan.buffers.premade.StaticMeshBuffer;

public interface VertexConsumer {
    void begin();
    void vertex(Vertex vertex);
    void index(int index);
    void end();

    void mesh(Mesh mesh);
}
