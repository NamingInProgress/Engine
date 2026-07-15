package com.vke.api.rendering.abstraction.renderer.pipeline.resource.other.array;

import com.vke.api.rendering.abstraction.renderer.data.ImageView;
import com.vke.api.rendering.abstraction.renderer.data.Sampler;
import com.vke.api.rendering.abstraction.renderer.data.Texture;
import com.vke.api.rendering.abstraction.renderer.pipeline.resource.ShaderResource;

public interface CISArrayResource extends ShaderResource {
    void set(int index, ImageView view, Sampler sampler);

    default void set(int index, Texture texture, Sampler sampler) {
        this.set(index, texture.defaultView(), sampler);
    }
}
