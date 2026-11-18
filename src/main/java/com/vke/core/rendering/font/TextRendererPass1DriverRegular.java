package com.vke.core.rendering.font;

import com.vke.api.assets.r.R;
import com.vke.api.rendering.abstraction.renderer.RenderSystem;
import com.vke.api.rendering.abstraction.renderer.pipeline.driver.PipelineDriver;
import com.vke.api.rendering.abstraction.renderer.pipeline.resource.buf.BufferResource;
import com.vke.api.rendering.abstraction.renderer.pipeline.resource.buf.FieldArrayResource;
import com.vke.core.rendering.transform.MatrixStack;

public class TextRendererPass1DriverRegular extends PipelineDriver {

    protected final BufferResource matrices;
    protected final FieldArrayResource buf;

    private MatrixStack matrixStack;

    public TextRendererPass1DriverRegular(RenderSystem sys) {
        super(sys, R.pipelines.get("vke:font_pass_1_regular.pipeline.json"));
        this.matrices = p.resource("u_TextMatrixStack1");
        this.buf = p.resource("u_TextMatrixStack1.modelMatrices");
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
        matrixStack = null;
    }
}
