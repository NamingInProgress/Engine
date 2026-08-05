package com.vke.core.rendering.font;

import com.vke.api.assets.r.R;
import com.vke.api.rendering.abstraction.renderer.RenderSystem;
import com.vke.api.rendering.abstraction.renderer.pipeline.PipelineDriver;
import com.vke.api.rendering.abstraction.renderer.pipeline.resource.buf.BufferResource;
import com.vke.api.rendering.abstraction.renderer.pipeline.resource.buf.ValueResource;
import com.vke.core.rendering.transform.MatrixStack;
import org.joml.Matrix4f;

public class TextRendererPass1DriverRegular extends PipelineDriver {

    private final BufferResource matrices;

    private MatrixStack matrixStack;

    public TextRendererPass1DriverRegular(RenderSystem sys) {
        super(sys, R.pipelines.get("vke:font_pass_1_regular.pipeline.json"));
        this.matrices = p.resource("u_MatrixStack");
    }

    public void setMatrixStack(MatrixStack stack) {
        this.matrixStack = stack;
    }

    @Override
    public void use() {
        matrices.write(writer -> matrixStack.upload(writer));
        bind();
        bindPushConstants();
        bindDescriptorSets();
    }
}
