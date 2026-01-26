package com.vke.core.rendering.buffer;

import com.vke.api.vulkan.buffer.Vertex;

import java.nio.IntBuffer;

public class MeshBuffer<T extends Vertex> {
    private StaticVertexBuffer<T> vertices;
    private IntBuffer indices;
}
