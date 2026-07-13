package com.vke.core.rendering.vertexconsumer;

import com.vke.api.draw.Vertex;
import com.vke.core.mesh.Mesh;
import com.vke.core.rendering.vulkan.service.VulkanRenderSystem;

public class FastVertexConsumer<T extends Vertex> extends AbstractVertexConsumer<T> {

    public FastVertexConsumer(VulkanRenderSystem sys, T template) {
        super(sys, template);
    }

    public FastVertexConsumer(VulkanRenderSystem sys, T template, int estVertexCount, int estIndexCount) {
        super(sys, template, estVertexCount, estIndexCount);
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
