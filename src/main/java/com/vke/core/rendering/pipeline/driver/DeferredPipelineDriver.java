package com.vke.core.rendering.pipeline.driver;

import com.vke.api.assets.AssetHandle;
import com.vke.api.rendering.abstraction.renderer.RenderSystem;
import com.vke.api.rendering.abstraction.renderer.pipeline.MaterialPipelineDriver;
import com.vke.api.rendering.abstraction.renderer.pipeline.Pipeline;
import com.vke.api.rendering.abstraction.renderer.pipeline.PipelineDriver;
import com.vke.api.rendering.abstraction.renderer.pipeline.RenderPipeline;
import com.vke.api.rendering.abstraction.renderer.pipeline.resource.buf.FieldArrayResource;

import java.io.IOException;
import java.nio.ByteBuffer;

public class DeferredPipelineDriver extends MaterialPipelineDriver {

    private final RenderPipeline p;
    private final FieldArrayResource local;

    private ByteBuffer mats;

    public DeferredPipelineDriver(RenderSystem context, AssetHandle<? extends Pipeline> pipeline) {
        super(context, pipeline);
        try {
            this.p = (RenderPipeline) pipeline.acquire(context);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        this.local = p.resource("transforms.local");
    }

    public void setLocal(ByteBuffer mat) {
        this.mats = mat;
    }

    @Override
    public void use() {
        local.write((slice) -> slice.putData(mats));
        bind();
        bindDescriptorSets();
        //bindPushConstants();
    }

}
