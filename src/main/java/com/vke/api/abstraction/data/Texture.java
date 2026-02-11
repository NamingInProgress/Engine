package com.vke.api.abstraction.data;

import com.vke.api.abstraction.descriptors.texture.TextureFormat;
import com.vke.utils.Disposable;

public interface Texture extends Disposable {

    record Description() {}

    int width();
    int height();
    int depth();

    TextureFormat format();
    int mipLevels();

    boolean isSwapchainImage();

    TextureView createView(TextureView.Description info);

}
