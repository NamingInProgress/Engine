package com.vke.core.rendering.post;

import com.vke.api.rendering.abstraction.draw.VertexConsumer;
import com.vke.api.rendering.abstraction.renderer.RenderSystem;
import com.vke.api.rendering.abstraction.renderer.commands.CommandBuffer;
import com.vke.api.rendering.abstraction.renderer.data.Texture;
import com.vke.api.rendering.abstraction.renderer.enums.LoadOp;
import com.vke.api.rendering.abstraction.renderer.enums.StoreOp;
import com.vke.api.rendering.abstraction.rendergraph.RenderPass;
import com.vke.core.Identifier;
import com.vke.core.rendering.graph.GraphContext;
import com.vke.core.rendering.graph.RenderPassInstance;
import com.vke.core.rendering.post.service.PostProcessManager;
import com.vke.core.services2.Services;
import com.vke.impl.vertex.FullscreenQuadVertex;
import com.vke.utils.Utils;

import java.util.ArrayList;
import java.util.Arrays;
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

        List<Identifier> toSkip = context.getPostDisabledStages();
        if (toSkip == null) toSkip = Utils.emptyImmList();

        int runs = 0;
        for (PostProcessEffect effect : effects) {
            if (toSkip.contains(effect.identifier)) continue;

            colorCopy.useInShader();

            cmd.beginRendering(new CommandBuffer.RenderingInfo(List.of(
                    new CommandBuffer.AttachmentInfo(color, LoadOp.CLEAR, StoreOp.STORE, new float[]{0.2f, 0.3f, 0.3f, 1.0f})
            ), null));

            effect.draw(cmd, context, fsqc, runs == 0 ? input : colorCopy);

            cmd.endRendering();

            var temp = colorCopy;
            colorCopy = color;
            color = temp;
            runs++;
        }

        if (runs % 2 == 0) {
            cmd.copyImageToImage(runs == 0 ? input : colorCopy, color, 0, 0, 0, 0);
        }
    }

    public static void disableStages(GraphContext context, String... strings) {
        context.setPostDisabledStages(Arrays.stream(strings).map(context::id).toList());
    }

    public static void disableStages(GraphContext context, Identifier... idents) {
        context.setPostDisabledStages(Arrays.stream(idents).toList());
    }

}
