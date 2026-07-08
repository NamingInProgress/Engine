package com.vke.api.rendering.abstraction;

import com.vke.api.rendering.FrameCounter;
import com.vke.api.rendering.abstraction.data.ITextureManager;
import com.vke.api.services2.PinnedService;
import com.vke.api.services2.Service;

public interface Renderer extends PinnedService {
    RenderDevice getDevice();
    FrameCounter getFrameCounter();
    ITextureManager textureManager();
}
