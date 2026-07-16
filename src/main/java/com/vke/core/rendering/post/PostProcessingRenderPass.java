package com.vke.core.rendering.post;

import com.vke.api.rendering.abstraction.draw.VertexConsumer;
import com.vke.api.rendering.abstraction.renderer.RenderSystem;
import com.vke.api.rendering.abstraction.renderer.commands.CommandBuffer;
import com.vke.api.rendering.abstraction.renderer.data.Texture;
import com.vke.api.rendering.abstraction.renderer.enums.LoadOp;
import com.vke.api.rendering.abstraction.renderer.enums.StoreOp;
import com.vke.api.rendering.abstraction.rendergraph.RenderPass;
import com.vke.core.rendering.graph.GraphContext;
import com.vke.core.rendering.graph.RenderPassInstance;
import com.vke.core.rendering.post.service.PostProcessManager;
import com.vke.core.services2.Services;
import com.vke.utils.io.Identifier;

import java.util.ArrayList;
import java.util.List;

public class PostProcessingRenderPass extends RenderPass {

    private final List<PostProcessEffect> effects;
    private VertexConsumer<FullscreenQuadVertex> fsqc;

    public PostProcessingRenderPass(RenderSystem renderSystem, RenderPassInstance instance, List<Identifier> stages) {
        super(renderSystem, instance);
        effects = resolveStages(stages);
    }

    private ArrayList<PostProcessEffect> resolveStages(List<Identifier> stages) {
        PostProcessManager postManager = renderSystem.service(Services.POST_PROCESS);
        ArrayList<PostProcessEffect> effects = new ArrayList<>();

        for (Identifier stage : stages) {
            PostEffectProvider provider = postManager.getEffect(stage);
            PostProcessEffect effect = provider.buildEffect(renderSystem, instance);
            effects.add(effect);
        }

        return effects;
    }

    @Override
    public void onLoad() {
        this.fsqc = renderSystem.renderer().getVertexConsumerProvider().get(FullscreenQuadVertex.TEMPLATE);
        this.effects.forEach(PostProcessEffect::onInitialize);
    }

    @Override
    public void execute(CommandBuffer cmd, GraphContext context) {
        Texture color = instance.getOutputTexture("output");
        Texture colorCopy = instance.getDynamicColorOutputTexture("output", "outputCopy");
        Texture input = instance.getInputTexture("input");

        for (int i = 0; i < effects.size(); i++) {
            colorCopy.useInShader();

            PostProcessEffect effect = effects.get(i);
            cmd.beginRendering(new CommandBuffer.RenderingInfo(List.of(
                    new CommandBuffer.AttachmentInfo(color, LoadOp.CLEAR, StoreOp.STORE, new float[]{0.2f, 0.3f, 0.3f, 1.0f})
            ), null));

            effect.draw(cmd, context, fsqc, i == 0 ? input : colorCopy);

            cmd.endRendering();

            var temp = colorCopy;
            colorCopy = color;
            color = temp;
        }

        if (effects.size() % 2 == 0) {
            cmd.copyImageToImage(colorCopy, color, 0, 0, 0, 0);
        }
    }

}
