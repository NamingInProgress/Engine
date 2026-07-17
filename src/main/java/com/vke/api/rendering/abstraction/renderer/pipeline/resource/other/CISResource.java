package com.vke.api.rendering.abstraction.renderer.pipeline.resource.other;

import com.vke.api.rendering.abstraction.renderer.data.ImageView;
import com.vke.api.rendering.abstraction.renderer.data.Sampler;
import com.vke.api.rendering.abstraction.renderer.data.Texture;
import com.vke.api.rendering.abstraction.renderer.pipeline.resource.ShaderResource;

public interface CISResource extends ShaderResource {
    void set(ImageView view, Sampler sampler);

    default void set(Texture texture, Sampler sampler) {
        this.set(texture.defaultView(), sampler);
    }
}
