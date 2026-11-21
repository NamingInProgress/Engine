package com.vke.impl.rendering.queue;

import com.vke.api.rendering.abstraction.renderer.RenderSystem;
import com.vke.api.rendering.abstraction.renderer.commands.CommandBuffer;
import com.vke.api.rendering.abstraction.renderer.pipeline.resource.buf.FieldArrayResource;
import com.vke.core.rendering.font.TextRenderer;
import com.vke.core.rendering.graph2.GraphTexture;
import com.vke.core.rendering.graph2.renderpass.RenderPass;
import com.vke.core.rendering.rp.BasicTextInstance;
import com.vke.core.rendering.rp2.queue.RenderQueue;
import com.vke.core.rendering.rp2.queue.RenderQueueExecutor;
import com.vke.core.rendering.rp2.queue.impl.BasicRenderQueue;
import org.joml.Matrix4f;

public class BasicTextExecutor extends RenderQueueExecutor<BasicTextInstance> {

    private GraphTexture colorOut;

    private GraphTexture depthOut, stencilOut;

    private TextRenderer tx;
    private FieldArrayResource buf;

    @Override
    public void onLoad(RenderSystem sys, RenderPass pass) {
        colorOut = pass.searchOutputTexture("colorOut");
        depthOut = pass.searchOutputTexture("depthOut");
        stencilOut = pass.searchOutputTexture("depthOut");

        this.tx = new TextRenderer(sys);
        this.buf = tx.getMatrixBuffer();
    }

    @Override
    public void acceptQueue(RenderQueue<BasicTextInstance> q, CommandBuffer cmd, RenderPass pass) {
        BasicRenderQueue<BasicTextInstance> queue = (BasicRenderQueue<BasicTextInstance>) q;

        int counter = 0;
        for (BasicTextInstance bti : queue.iterator()) {
            tx.overrideMatrixIndex(counter);
            buf.write(counter++, (writer) -> writer.mat4(bti.mat));
            tx.color(bti.color);
            tx.drawText(bti.font, bti.fontScale, bti.selector, bti.text);
        }

        if (counter != 0) tx.render(cmd, pass, colorOut, stencilOut, depthOut);
    }
}
