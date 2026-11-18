package com.vke.test.ttf;

import com.vke.api.assets.AssetHandle;
import com.vke.api.assets.r.R;
import com.vke.api.rendering.abstraction.renderer.RenderSystem;
import com.vke.api.rendering.abstraction.renderer.commands.CommandBuffer;
import com.vke.api.rendering.abstraction.renderer.data.Texture;
import com.vke.api.rendering.abstraction.renderer.enums.LoadOp;
import com.vke.api.rendering.abstraction.renderer.enums.StoreOp;
import com.vke.api.rendering.abstraction.renderer.pipeline.RenderPipeline;
import com.vke.api.rendering.abstraction.renderer.pipeline.resource.buf.BufferResource;
import com.vke.api.rendering.abstraction.renderer.pipeline.resource.buf.ValueResource;
import com.vke.core.color.RgbColor;
import com.vke.core.rendering.font.TextRenderer;
import com.vke.core.rendering.graph2.GraphContext;
import com.vke.core.rendering.graph2.GraphTexture;
import com.vke.core.rendering.graph2.RenderGraph;
import com.vke.core.rendering.graph2.renderpass.RenderPass;
import org.joml.Matrix4f;

import java.io.IOException;
import java.util.List;

public class TTFRenderPass extends RenderPass {

    private GraphTexture colorOut, stencil, depth;

    public TTFRenderPass(RenderSystem sys, RenderGraph graph, Def def) {
        super(sys, graph, def);
    }


    @Override
    public void onLoad() {
        //            this.pip = R.pipelines.get("vke:font_pass_1_bezier.pipeline.json");
//            this.pip2 = R.pipelines.get("vke:font_pass_1_regular.pipeline.json");
//            this.pip2 = R.pipelines.get("vke:font_pass_2.pipeline.json");
//
//            RenderPipeline p = pip.acquire(sys);
//            RenderPipeline p2 = pip2.acquire(sys);
//            RenderPipeline p3 = pip3.acquire(sys);
//
//            this.matrixStack = p.resource("matrixStack");
//            this.proj = p.resource("world");
//
//            this.mxs2 = p2.resource("matrixStack");
//            this.proj2 = p2.resource("world");
//
//            this.mxs3 = p3.resource("matrixStack");
//            this.proj3 = p3.resource("world");
//
//            this.proj.write(s -> s.mat4(new Matrix4f().ortho(0, 800, 0, 600, 0, 1000, true)));
//            this.proj2.write(s -> s.mat4(new Matrix4f().ortho(0, 800, 0, 600, 0, 1000, true)));
//            this.proj3.write(s -> s.mat4(new Matrix4f().ortho(0, 800, 0, 600, 0, 1000, true)));

        this.colorOut = searchOutputTexture("colorOut");
        this.stencil = searchOutputTexture("depthStencil");
        this.depth = searchOutputTexture("depthStencil");
    }

    @Override
    public void execute(CommandBuffer cmd, GraphContext context) {
        Texture colorOut = this.colorOut.extract();

        Texture stencil = this.stencil.extract();
        Texture depth = this.depth.extract();

        TextRenderer tx = context.get("tx");

        //tx.render(cmd, new CommandBuffer.AttachmentInfo(colorOut, LoadOp.CLEAR, StoreOp.STORE, RgbColor.VKE.getComponents()), stencil, depth);
    }
}
