package com.vke.core.rendering.graph2.renderpass;

import com.vke.api.parsing.config.node.ConfigArrayNode;
import com.vke.api.parsing.config.node.ConfigNode;
import com.vke.api.rendering.abstraction.renderer.RenderSystem;
import com.vke.api.rendering.abstraction.renderer.commands.CommandBuffer;
import com.vke.api.rendering.abstraction.renderer.data.Texture;
import com.vke.api.rendering.abstraction.renderer.enums.LoadOp;
import com.vke.api.rendering.abstraction.renderer.enums.StoreOp;
import com.vke.core.Identifier;
import com.vke.core.rendering.graph2.GraphContext;
import com.vke.core.rendering.graph2.GraphTexture;
import com.vke.core.rendering.graph2.RenderGraph;
import com.vke.core.rendering.post.PostEffectProvider;
import com.vke.core.rendering.post.PostProcessEffect;
import com.vke.core.rendering.post.service.PostProcessManager;
import com.vke.core.services2.Services;
import com.vke.utils.Utils;

import java.util.List;

public class PostEffectRenderPass extends DataRenderPass {
    private final Identifier[] stages;
    private final PostProcessEffect[] effects;

    private GraphTexture colorOut, colorOutPing, colorIn;

    public PostEffectRenderPass(RenderSystem sys, RenderGraph graph, Def def, ConfigArrayNode data) {
        super(sys, graph, def, data);

        ConfigArrayNode stages = data.getArray("stages");
        ConfigNode[] stageArray = stages.values();

        this.stages = new Identifier[stageArray.length];
        this.effects = new PostProcessEffect[stageArray.length];

        PostProcessManager postManager = sys.service(Services.POST_PROCESS);

        for (int i = 0; i < stageArray.length; i++) {
            ConfigNode stage =  stageArray[i];
            String nameString = stage.getStringOption("name").unwrapOrPanic(new IllegalStateException("Cannot have stage without name tag!"));
            Identifier stageIdent = sys.id(nameString);
            this.stages[i] = stageIdent;

            PostEffectProvider provider = postManager.getEffect(stageIdent);
            //we have to call this here before onLoad so that effects can add textures and shit
            PostProcessEffect effect = provider.buildEffect(sys, this);
            effects[i] = effect;
        }
    }

    @Override
    public void onLoad() {
        colorOut = searchOutputTexture("colorOut");
        colorOutPing = searchOutputTexture("colorOutPing");
        colorIn = searchInputTexture("colorIn");

        for (PostProcessEffect effect : effects) {
            effect.onInitialize();
        }
    }

    @Override
    public void execute(CommandBuffer cmd, GraphContext context) {
        Texture color = colorOut.extract();
        Texture colorCopy = colorOutPing.extract();
        Texture input = colorIn.extract();

        List<Identifier> toSkip = context.getPostDisabledStages();
        if (toSkip == null) toSkip = Utils.emptyImmList();

        int runs = 0;
        for (int i = 0; i < effects.length; i++) {
            PostProcessEffect effect = effects[i];
            Identifier identifier = stages[i];
            if (toSkip.contains(identifier)) continue;

            colorCopy.useInShader();

            if (effect.autoStartRendering()) {
                cmd.beginRendering(new CommandBuffer.RenderingInfo(List.of(
                        new CommandBuffer.AttachmentInfo(color, LoadOp.CLEAR, StoreOp.STORE, new float[]{0.2f, 0.3f, 0.3f, 1.0f})
                ), null));
            }

            effect.draw(cmd, context, runs == 0 ? input : colorCopy, color);

            if (effect.autoStartRendering()) {
                cmd.endRendering();
            }

            var temp = colorCopy;
            colorCopy = color;
            color = temp;
            runs++;
        }

        if (runs % 2 == 0) {
            cmd.copyImageToImage(runs == 0 ? input : colorCopy, color, 0, 0, 0, 0);
        }
    }
}
