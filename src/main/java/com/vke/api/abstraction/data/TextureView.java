package com.vke.api.abstraction.data;

import com.vke.api.abstraction.descriptors.texture.TextureFormat;
import com.vke.api.abstraction.descriptors.texture.TextureViewType;
import com.vke.utils.Disposable;

public interface TextureView extends Disposable {

    record Description() {}

    Texture parent();
    TextureFormat format();
    TextureViewType type();

    int baseMip();
    int mipCount();

    int baseLayer();
    int layerCount();

}
