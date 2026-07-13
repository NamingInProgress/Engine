package com.vke.core.rendering.pipeline.driver;

import com.vke.api.assets.AssetHandle;
import com.vke.api.game.camera.Camera;
import com.vke.api.rendering.abstraction.RenderSystem;
import com.vke.api.rendering.abstraction.pipeline.Pipeline;
import com.vke.api.rendering.abstraction.pipeline.PipelineDriver;
import com.vke.api.rendering.vulkan.pushconstants.PushConstantHandle;
import com.vke.core.rendering.vulkan.pipeline.VulkanRenderPipeline;
import org.joml.Matrix4f;

import java.io.IOException;

public class DemoPipelineDriver extends PipelineDriver {

    private final VulkanRenderPipeline p;
    private final PushConstantHandle projection, view, local;

    private Matrix4f mat;

    private Camera camera;

    public DemoPipelineDriver(RenderSystem context, AssetHandle<? extends Pipeline> pipeline) {
        super(context, pipeline);
        try {
            this.p = (VulkanRenderPipeline) pipeline.acquire(context);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        this.projection = p.resolvePushConstant("projection");
        this.local = p.resolvePushConstant("local");
        this.view = p.resolvePushConstant("view");
    }

    public void setCamera(Camera camera) {
        this.camera = camera;
    }

    public void setLocal(Matrix4f mat) {
        this.mat = mat;
    }

    @Override
    public void use() {
        bind();
        bindDescriptorSets();
        projection.write((slice) -> slice.putMat4(camera.projectionMatrix()));
        view.write((slice) -> slice.putMat4(camera.viewMatrix()));
        local.write((slice) -> slice.putMat4(mat));
        bindPushConstants();
    }

}
