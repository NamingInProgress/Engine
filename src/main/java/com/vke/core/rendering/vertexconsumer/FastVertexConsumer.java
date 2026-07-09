package com.vke.core.rendering.vertexconsumer;

import com.vke.api.draw.Vertex;
import com.vke.core.VKEngine;
import com.vke.core.mesh.Mesh;
import com.vke.core.vulkan.service.VulkanRenderer;

public class FastVertexConsumer<T extends Vertex> extends AbstractVertexConsumer<T> {

    public FastVertexConsumer(VKEngine engine, VulkanRenderer renderer, T template) {
        super(engine, renderer, template);
    }

    public FastVertexConsumer(VKEngine engine, VulkanRenderer renderer, T template, int estVertexCount, int estIndexCount) {
        super(engine, renderer, template, estVertexCount, estIndexCount);
    }

    @Override
    public void vertices(T... vertices) {
        putVertices(vertices);
    }

    @Override
    public void indices(int... indices) {
        putIndices(indices);
    }

    @Override
    public void mesh(Mesh<T> mesh) {
        putMesh(mesh);
    }
}
