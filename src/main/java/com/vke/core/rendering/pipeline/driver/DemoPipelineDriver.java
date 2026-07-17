package com.vke.core.rendering.pipeline.driver;

import com.vke.api.assets.AssetHandle;
import com.vke.api.game.camera.Camera;
import com.vke.api.rendering.abstraction.renderer.RenderSystem;
import com.vke.api.rendering.abstraction.renderer.pipeline.Pipeline;
import com.vke.api.rendering.abstraction.renderer.pipeline.PipelineDriver;
import com.vke.api.rendering.abstraction.renderer.pipeline.RenderPipeline;
import com.vke.api.rendering.abstraction.renderer.pipeline.resource.ShaderResource;
import com.vke.api.rendering.abstraction.renderer.pipeline.resource.buf.BufferResource;
import com.vke.api.rendering.abstraction.renderer.pipeline.resource.buf.FieldResource;
import com.vke.api.rendering.abstraction.renderer.pipeline.resource.buf.ValueResource;
import com.vke.api.rendering.vulkan.pushconstants.PushConstantHandle;
import com.vke.core.rendering.vulkan.pipeline.VulkanRenderPipeline;
import org.joml.Matrix4f;

import java.io.IOException;

public class DemoPipelineDriver extends PipelineDriver {

    private final RenderPipeline p;
    private final ValueResource local;

    private Matrix4f mat;

    private Camera camera;

    public DemoPipelineDriver(RenderSystem context, AssetHandle<? extends Pipeline> pipeline) {
        super(context, pipeline);
        try {
            this.p = (RenderPipeline) pipeline.acquire(context);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        this.local = p.resource("local");
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
        local.write((slice) -> slice.putMat4(mat));
        bindPushConstants();
    }

}
