package com.vke.core.rendering.graph2;

import com.vke.api.rendering.abstraction.renderer.RenderSystem;
import com.vke.api.rendering.abstraction.renderer.data.Texture;
import com.vke.core.rendering.graph2.renderpass.RenderPass;

public class GraphTexture {
    private final RenderSystem system;
    private final TextureCategory cat;
    private final RenderPass renderPass;
    private final int index;

    public GraphTexture(RenderSystem system, TextureCategory cat, RenderPass renderPass, int index) {
        this.system = system;
        this.cat = cat;
        this.renderPass = renderPass;
        this.index = index;
    }

    public Texture extract() {
        if (cat == TextureCategory.Input) {
            return renderPass.getInputTexture(index);
        } else {
            return renderPass.getOutputTexture(index, system.getFrameCounter().currentIndex());
        }
    }

    public int getIndex() {
        return index;
    }
}
