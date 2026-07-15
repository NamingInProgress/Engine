package com.vke.demo;

import com.vke.api.assets.r.R;
import com.vke.api.rendering.abstraction.renderer.RenderResourceManager;
import com.vke.api.rendering.abstraction.renderer.RenderSystem;
import com.vke.api.rendering.abstraction.renderer.commands.CommandBuffer;
import com.vke.api.rendering.abstraction.renderer.data.StaticMesh;
import com.vke.api.rendering.abstraction.renderer.data.Texture;
import com.vke.api.rendering.abstraction.renderer.enums.LoadOp;
import com.vke.api.rendering.abstraction.renderer.enums.StoreOp;
import com.vke.api.rendering.abstraction.rendergraph.RenderGraph;
import com.vke.api.rendering.abstraction.rendergraph.RenderPass;
import com.vke.api.rendering.abstraction.rendergraph.RenderPassInstance;
import com.vke.core.geometry.Rect;
import com.vke.core.mesh.MeshPrefab;
import com.vke.core.rendering.pipeline.RenderPipelines;
import org.joml.Matrix4f;

import java.io.IOException;
import java.util.List;

public class DemoRenderPass extends RenderPass {
    public DemoRenderPass(RenderSystem context, RenderPassInstance instance) {
        super(context, instance);
    }

    @Override
    public void execute(CommandBuffer cmd, RenderGraph graph) {
        Texture color = instance.getOutputTexture("render-target");
        Texture depth = instance.getOutputTexture("depthOut");
        cmd.beginRendering(new CommandBuffer.RenderingInfo(List.of(
                    new CommandBuffer.AttachmentInfo(color, LoadOp.CLEAR, StoreOp.STORE, new float[]{ 0.2f, 0.3f, 0.3f, 1.0f })
                ),
                new CommandBuffer.AttachmentInfo(depth, LoadOp.CLEAR, StoreOp.STORE, new float[]{ 1.0f })));

        Matrix4f model = new Matrix4f();

        float time = (System.nanoTime() / 1_000_000_000.0f);

        float speed = 1.0f;

        float scale = 10;
        model.identity()
                .translate(0, 0.0f, -550)
                .scale(scale, scale, scale)
                .rotateY(time * speed);

        RenderPipelines.DEMO.setLocal(model);
        RenderPipelines.DEMO.use();
        DemoScene.MESH.draw();

        cmd.endRendering();
    }

}
