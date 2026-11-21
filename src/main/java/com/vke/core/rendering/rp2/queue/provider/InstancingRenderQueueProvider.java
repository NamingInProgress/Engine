package com.vke.core.rendering.rp2.queue.provider;

import com.vke.core.rendering.rp2.queue.RenderQueue;
import com.vke.core.rendering.rp2.queue.RenderQueueProvider;
import com.vke.core.rendering.rp2.queue.impl.InstancingRenderQueue;

public class InstancingRenderQueueProvider implements RenderQueueProvider {
    @Override
    public RenderQueue<?> get() {
        return new InstancingRenderQueue<>();
    }
}
