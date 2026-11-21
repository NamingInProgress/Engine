package com.vke.core.rendering.rp2.queue.provider;

import com.vke.core.rendering.rp2.queue.RenderQueue;
import com.vke.core.rendering.rp2.queue.RenderQueueProvider;
import com.vke.core.rendering.rp2.queue.impl.BasicRenderQueue;

public class BasicTextRenderQueueProvider implements RenderQueueProvider {
    @Override
    public RenderQueue<?> get() {
        return new BasicRenderQueue<>();
    }
}
