package com.vke.api.rendering.abstraction;

import com.vke.api.rendering.FrameCounter;
import com.vke.api.rendering.abstraction.draw.VertexConsumerProvider;
import com.vke.api.services2.PinnedService;
import com.vke.api.services2.Service;

public interface Renderer extends PinnedService {
    RenderDevice getDevice();
    FrameCounter getFrameCounter();
    RenderSystem renderSystem(); // Bridge to lower level
    RenderResourceManager resourceManager(); // Example implementation: resourceManager = new VulkanResourceManager(renderContext());

    VertexConsumerProvider getVertexConsumerProvider();
}
