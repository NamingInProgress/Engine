package com.vke.api.abstraction.data;

import com.vke.api.abstraction.descriptors.texture.ImageUsage;
import com.vke.api.abstraction.descriptors.texture.TextureFormat;
import com.vke.core.vulkan.extent.Extent2D;
import com.vke.core.vulkan.extent.Extent3D;
import com.vke.utils.Disposable;

import java.io.InputStream;

public interface Texture extends Disposable {

    record Description(TextureFormat format, Extent3D extent, ImageUsage usageFlags) {

        public Description(TextureFormat format, Extent2D ext, ImageUsage usageFlags) {
            this(format, new Extent3D(ext, 1), usageFlags);
        }

    }

    void loadImage(InputStream data);

    int width();
    int height();
    int depth();

    TextureFormat format();
    int mipLevels();

    boolean isSwapchainImage();

    TextureView createView(TextureView.Description info);

    long getHandle();

}
