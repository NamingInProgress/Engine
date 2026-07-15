package com.vke.api.rendering.abstraction.renderer.pipeline.resource.other;

import com.vke.api.rendering.abstraction.renderer.data.ImageView;
import com.vke.api.rendering.abstraction.renderer.data.Texture;
import com.vke.api.rendering.abstraction.renderer.pipeline.resource.ShaderResource;

public interface ImageResource extends ShaderResource {
    void set(ImageView view);

    default void set(Texture texture) {
        this.set(texture.defaultView());
    }
}
