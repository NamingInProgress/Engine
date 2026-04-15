package com.vke.core.vulkan.vertexconsumer;

import com.vke.api.assets.AssetHandle;
import com.vke.api.draw.Vertex;
import com.vke.api.rendering.abstraction.pipeline.RenderPipeline;
import com.vke.api.rendering.vulkan.descriptors.handles.array.CombinedImageSamplerArrayHandle;
import com.vke.core.Context;
import com.vke.core.mesh.Mesh;
import com.vke.core.vulkan.VulkanRenderer;
import com.vke.core.vulkan.pipeline.VulkanRenderPipeline;

import java.io.IOException;
import java.util.ArrayList;

public class BatchedVKVertexConsumer<T extends Vertex> extends AbstractVertexConsumer<T> {

    private final AssetHandle<RenderPipeline> pipeline;
    private final CombinedImageSamplerArrayHandle texHandle;

    //private final ArrayList<Batch> batches = new ArrayList<>();

    public BatchedVKVertexConsumer(Context context, VulkanRenderer renderer, T template, AssetHandle<RenderPipeline> pipeline,
                                   String texturesArrayUniformName) {
        this(context, renderer, template, pipeline, texturesArrayUniformName, BASE_VERTEX_COUNT, BASE_INDEX_COUNT);
    }

    public BatchedVKVertexConsumer(Context context, VulkanRenderer renderer, T template, AssetHandle<RenderPipeline> pipeline,
                                   String texturesArrayUniformName, int estVertexCount, int estIndexCount) {
        super(context.getEngine(), renderer, template, estVertexCount, estIndexCount);
        this.pipeline = pipeline;

        try {
            this.texHandle = ((VulkanRenderPipeline) pipeline.acquire(context)).resolveUniform(texturesArrayUniformName);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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
