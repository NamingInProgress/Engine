package com.vke.core.vulkan.vertexconsumer;

import com.vke.api.draw.Vertex;
import com.vke.core.VKEngine;
import com.vke.core.mesh.Mesh;
import com.vke.core.vulkan.VulkanRenderer;

public class BatchedVertexConsumer<T extends Vertex> extends AbstractVertexConsumer<T> {

    public BatchedVertexConsumer(VKEngine engine, VulkanRenderer renderer, T template) {
        super(engine, renderer, template);
    }

    public BatchedVertexConsumer(VKEngine engine, VulkanRenderer renderer, T template, int estVertexCount, int estIndexCount) {
        super(engine, renderer, template, estVertexCount, estIndexCount);
    }

    @Override
    public void vertices(T... vertices) {

    }

    @Override
    public void indices(int... indices) {

    }

    @Override
    public void mesh(Mesh<T> mesh) {

    }
}
