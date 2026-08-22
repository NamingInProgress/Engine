package com.vke.impl.driver;

import com.vke.api.assets.r.R;
import com.vke.api.rendering.abstraction.renderer.RenderSystem;
import com.vke.api.rendering.abstraction.renderer.pipeline.PipelineDriver;
import com.vke.api.rendering.abstraction.renderer.pipeline.resource.buf.BufferResource;
import com.vke.core.rendering.transform.MatrixStack;

public class ShapePipelineDriver extends PipelineDriver {
    private MatrixStack stack;
    private BufferResource transforms;

    public ShapePipelineDriver(RenderSystem sys) {
        super(sys, R.pipelines.get("shape.pipeline.json"));
        this.transforms = p.resource("transforms");
    }

    public void setMatrices(MatrixStack stack) {
        this.stack = stack;
    }

    @Override
    public void use() {
        bind();
        bindDescriptorSets();
        if (stack != null) {
            transforms.write(stack::upload);
        }
    }
}
