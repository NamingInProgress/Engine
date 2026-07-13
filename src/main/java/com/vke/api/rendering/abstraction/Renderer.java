package com.vke.api.rendering.abstraction;

import com.vke.api.rendering.FrameCounter;
import com.vke.api.rendering.abstraction.data.IFrameDataManager;
import com.vke.api.rendering.abstraction.data.ITextureManager;
import com.vke.api.rendering.abstraction.data.Texture;
import com.vke.api.rendering.abstraction.draw.VertexConsumerProvider;
import com.vke.api.services2.Service;

public interface Renderer extends Service {
    RenderDevice getDevice();
    FrameCounter getFrameCounter();

    VertexConsumerProvider getVertexConsumerProvider();
}
