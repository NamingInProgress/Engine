package com.vke.api.rendering.abstraction.renderer.data;

public interface TextureManager {

    int registerTexture(Texture tex);
    int texture(Texture tex);
    void removeTexture(Texture tex);
    void withSampler(Sampler sampler);

}
