package com.vke.api.rendering.abstraction.data;

public interface ITextureManager {

    int registerTexture(Texture tex);
    int texture(Texture tex);
    void removeTexture(Texture tex);

}
