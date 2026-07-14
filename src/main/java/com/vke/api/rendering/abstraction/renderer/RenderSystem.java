package com.vke.api.rendering.abstraction.renderer;

import com.vke.api.rendering.FrameCounter;
import com.vke.api.rendering.abstraction.renderer.commands.CommandBuffer;
import com.vke.api.rendering.abstraction.renderer.data.FrameDataManager;
import com.vke.api.rendering.abstraction.renderer.data.TextureManager;
import com.vke.api.rendering.abstraction.renderer.swapchain.Swapchain;
import com.vke.core.Context;
import com.vke.core.ContextWrapper;

public abstract class RenderSystem extends ContextWrapper {

    public RenderSystem(Context baseContext) {
        super(baseContext);
    }

    public abstract Renderer renderer();
    public abstract RenderDevice device();
    public abstract Swapchain swapchain();

    public abstract TextureManager textureManager();
    public abstract FrameDataManager frameDataManager();

    public abstract CommandBuffer getCurrentCommandBuffer();

    public abstract FrameCounter getFrameCounter();

    public abstract long windowHandle();

    public abstract boolean zZeroToOne();

}
