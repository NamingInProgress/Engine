package com.vke.core.rendering.font;

import com.vke.api.assets.r.R;
import com.vke.api.rendering.abstraction.renderer.RenderSystem;
import com.vke.api.rendering.abstraction.renderer.pipeline.driver.PipelineDriver;
import com.vke.api.rendering.abstraction.renderer.pipeline.resource.buf.BufferResource;
import com.vke.core.rendering.transform.MatrixStack;

public class TextRendererPass2Driver extends PipelineDriver {

    protected final BufferResource matrices;

    private MatrixStack matrixStack;

    public TextRendererPass2Driver(RenderSystem sys) {
        super(sys, R.pipelines.get("vke:font_pass_2.pipeline.json"));
        this.matrices = p.resource("u_TextMatrixStack1");
    }

    public void setMatrixStack(MatrixStack stack) {
        this.matrixStack = stack;
    }

    @Override
    public void use() {
        if (matrixStack != null)
            matrices.write(writer -> matrixStack.upload(writer));
        bind();
        bindPushConstants();
        bindDescriptorSets();
    }
}
