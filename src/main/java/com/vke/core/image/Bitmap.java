package com.vke.core.image;

import com.vke.api.rendering.abstraction.renderer.RenderDevice;
import com.vke.api.rendering.abstraction.renderer.Renderer;
import com.vke.api.rendering.abstraction.renderer.data.Texture;
import com.vke.api.rendering.abstraction.renderer.enums.texture.Format;
import com.vke.api.rendering.abstraction.renderer.enums.texture.TextureType;

import java.nio.ByteBuffer;

public interface Bitmap {
    Format dataFormat();
    int width();
    int height();
    boolean hasAlphaChannel();
    int pixelStride();

    default Texture createTexture2D(Renderer renderer, Texture.TextureDescriptorBuilder partiallyFilledBuilder) {
        RenderDevice device = renderer.getDevice();
        Texture.TextureDesc desc = partiallyFilledBuilder
                .format(dataFormat())
                .width(width())
                .height(height())
                .type(TextureType.TEX_2D)
                .build();

        Texture texture = device.createTexture(desc);
        texture.upload(this);
        return texture;
    }

    void uploadIntoBytebuffer(ByteBuffer buffer);
}
