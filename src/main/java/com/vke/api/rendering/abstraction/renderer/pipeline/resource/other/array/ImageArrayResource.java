package com.vke.api.rendering.abstraction.renderer.pipeline.resource.other.array;

import com.vke.api.rendering.abstraction.renderer.data.ImageView;
import com.vke.api.rendering.abstraction.renderer.data.Texture;
import com.vke.api.rendering.abstraction.renderer.pipeline.resource.ShaderResource;

public interface ImageArrayResource extends ShaderResource {
    void set(int index, ImageView view);

    default void set(int index, Texture texture) {
        this.set(index, texture.defaultView());
    }
}
