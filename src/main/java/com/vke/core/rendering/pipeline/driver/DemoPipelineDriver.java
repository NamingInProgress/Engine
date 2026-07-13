package com.vke.core.rendering.pipeline.driver;

import com.vke.api.assets.AssetHandle;
import com.vke.api.rendering.abstraction.RenderSystem;
import com.vke.api.rendering.abstraction.pipeline.Pipeline;
import com.vke.api.rendering.abstraction.pipeline.PipelineDriver;
import com.vke.api.rendering.vulkan.pushconstants.PushConstantHandle;
import com.vke.core.Context;
import com.vke.core.vulkan.pipeline.VulkanRenderPipeline;
import com.vke.core.vulkan.service.VulkanRenderSystem;
import org.joml.Matrix4f;

import java.io.IOException;

public class DemoPipelineDriver extends PipelineDriver {

    private final VulkanRenderPipeline p;
    private final PushConstantHandle projection, local;

    private Matrix4f mat;

    public DemoPipelineDriver(RenderSystem context, AssetHandle<? extends Pipeline> pipeline) {
        super(context, pipeline);
        try {
            this.p = (VulkanRenderPipeline) pipeline.acquire(context);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        this.projection = p.resolvePushConstant("projection");
        this.local = p.resolvePushConstant("local");
    }

    public void setLocal(Matrix4f mat) {
        this.mat = mat;
    }

    @Override
    public void use() {
        bind();
        bindDescriptorSets();
        projection.write((slice) -> slice.putMat4(new Matrix4f().setPerspective((float) Math.toRadians(90),
                (float) 800 / 600, 0.1f, 1000, true)));
        local.write((slice) -> slice.putMat4(mat));
        bindPushConstants();
    }

}
