package com.vke.api.rendering.abstraction.renderer;

import com.vke.api.rendering.FrameCounter;
import com.vke.api.rendering.abstraction.draw.VertexConsumerProvider;
import com.vke.api.services2.PinnedService;

public interface Renderer extends PinnedService {
    RenderDevice getDevice();
    FrameCounter getFrameCounter();
    RenderSystem renderSystem(); // Bridge to lower level
    RenderResourceManager resourceManager();

    VertexConsumerProvider getVertexConsumerProvider();

    void beforeTerminate();
}
