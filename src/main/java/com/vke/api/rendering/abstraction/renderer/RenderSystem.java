package com.vke.api.rendering.abstraction.renderer;

import com.vke.api.rendering.FrameCounter;
import com.vke.api.rendering.abstraction.draw.VertexConsumerProvider;
import com.vke.api.rendering.abstraction.light.LightManager;
import com.vke.api.rendering.abstraction.renderer.commands.CommandBuffer;
import com.vke.api.rendering.abstraction.renderer.data.FrameDataManager;
import com.vke.api.rendering.abstraction.renderer.data.MaterialManager;
import com.vke.api.rendering.abstraction.renderer.data.RenderingEncoder;
import com.vke.api.rendering.abstraction.renderer.data.TextureManager;
import com.vke.api.rendering.abstraction.renderer.swapchain.Swapchain;
import com.vke.core.Context;
import com.vke.core.ContextWrapper;
import com.vke.core.rendering.vulkan.buffers.premade.slice.BufferSlice;

import java.nio.ByteBuffer;

public abstract class RenderSystem extends ContextWrapper {

    public RenderSystem(Context baseContext) {
        super(baseContext);
    }

    public VertexConsumerProvider vcp() {
        return renderer().getVertexConsumerProvider();
    }

    public abstract Renderer renderer();
    public abstract RenderDevice device();
    public abstract Swapchain swapchain();

    public abstract TextureManager textureManager();
    public abstract FrameDataManager frameDataManager();
    public abstract MaterialManager materialManager();
    public abstract LightManager lightManager();

    public abstract CommandBuffer getCurrentCommandBuffer();

    public abstract FrameCounter getFrameCounter();

    public abstract RenderingEncoder createRenderingEncoder(ByteBuffer buf);

    public abstract long windowHandle();

    public abstract boolean zZeroToOne();
    public abstract boolean flipImages();

}
