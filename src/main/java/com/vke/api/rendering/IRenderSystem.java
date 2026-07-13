package com.vke.api.rendering;

import com.vke.api.rendering.abstraction.RenderDevice;
import com.vke.api.rendering.abstraction.Renderer;
import com.vke.api.rendering.abstraction.data.IFrameDataManager;
import com.vke.api.rendering.abstraction.data.ITextureManager;
import com.vke.api.rendering.abstraction.swapchain.Swapchain;

public interface IRenderSystem {

    Renderer renderer();
    RenderDevice device();
    Swapchain swapchain();

    ITextureManager textureManager();
    IFrameDataManager frameDataManager();

    FrameCounter getFrameCounter();

    long windowHandle();

}
