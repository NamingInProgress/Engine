package com.vke.api.rendering.abstraction.rendergraph;

import com.vke.api.rendering.abstraction.renderer.RenderSystem;
import com.vke.api.rendering.abstraction.renderer.commands.CommandBuffer;
import com.vke.api.rendering.abstraction.renderer.enums.LoadOp;
import com.vke.api.rendering.abstraction.renderer.enums.StoreOp;
import com.vke.core.color.Color;
import com.vke.core.color.OldColor;
import com.vke.core.rendering.graph.GraphContext;
import com.vke.core.rendering.graph.RenderPassInstance;

import java.util.ArrayList;
import java.util.List;

public abstract class RenderPass {

    protected final RenderSystem renderSystem;
    protected final RenderPassInstance instance;

    public RenderPass(RenderSystem renderSystem, RenderPassInstance instance) {
        this.renderSystem = renderSystem;
        this.instance = instance;
    }

    public void onLoad() {}

    public void beginRendering(CommandBuffer cmd, List<String> color, OldColor clear) {
        this.beginRendering(cmd, color, null, null, List.of(clear), null, null);
    }

    public void beginRendering(CommandBuffer cmd, List<String> color, String depth, OldColor clear, OldColor depthClear) {
        this.beginRendering(cmd, color, depth, null, List.of(clear), depthClear, null);
    }

    public void beginRendering(CommandBuffer cmd, List<String> color, String depth, List<OldColor> clear, OldColor depthClear) {
        this.beginRendering(cmd, color, depth, null, clear, depthClear, null);
    }

    public void beginRendering(CommandBuffer cmd, List<String> color, String depth, String stencil, List<OldColor> clear, OldColor depthClear, OldColor stencilClear) {
        List<CommandBuffer.AttachmentInfo> colorInfos = new ArrayList<>();
        CommandBuffer.AttachmentInfo da = null, sa = null;
        for (int i = 0; i < color.size(); i++) {
            String s = color.get(i);
            colorInfos.add(new CommandBuffer.AttachmentInfo(instance.getOutputTexture(s), getLoadOp(s), StoreOp.STORE, clear.get(i).toFloat()));
        }

        if (depth != null) {
            da = new CommandBuffer.AttachmentInfo(instance.getOutputTexture(depth), getLoadOp(depth), StoreOp.STORE, depthClear.toFloat());
        }

        if (stencil != null) {
            sa = new CommandBuffer.AttachmentInfo(instance.getOutputTexture(stencil), getLoadOp(stencil), StoreOp.STORE, stencilClear.toFloat());
        }

        cmd.beginRendering(new CommandBuffer.RenderingInfo(colorInfos, da, sa));
    }

    public LoadOp getLoadOp(String s) {
        if (instance.outputHasSource(s)) return LoadOp.LOAD;
        return LoadOp.CLEAR;
    }

    public abstract void execute(CommandBuffer cmd, GraphContext context);

}
