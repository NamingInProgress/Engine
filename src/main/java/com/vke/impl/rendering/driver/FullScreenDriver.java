package com.vke.impl.rendering.driver;

import com.vke.api.assets.r.R;
import com.vke.api.rendering.abstraction.renderer.RenderSystem;
import com.vke.api.rendering.abstraction.renderer.data.ImageView;
import com.vke.api.rendering.abstraction.renderer.data.Sampler;
import com.vke.api.rendering.abstraction.renderer.data.Texture;
import com.vke.api.rendering.abstraction.renderer.pipeline.PipelineDriver;
import com.vke.api.rendering.abstraction.renderer.pipeline.resource.other.CISResource;
import com.vke.core.rendering.Samplers;

public class FullScreenDriver extends PipelineDriver {

    private final CISResource inTex;

    private Sampler sampler;
    private ImageView view;

    public FullScreenDriver(RenderSystem sys) {
        super(sys, R.pipelines.get("full_screen_pass.pipeline.json"));
        this.inTex = p.resource("u_InTex");
        this.sampler = Samplers.LINEAR;
    }

    public void texture(Texture tex) {
        this.texture(tex.defaultView());
    }

    public void texture(ImageView view) {
        this.view = view;
    }

    public void sampler(Sampler sampler) {
        this.sampler = sampler;
    }

    @Override
    public void use() {
        inTex.set(view, sampler);
        bind();
        bindDescriptorSets();
    }
}
