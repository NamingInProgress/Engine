package com.vke.api.rendering.abstraction;

import com.vke.api.rendering.FrameCounter;
import com.vke.api.services2.Service;

public interface Renderer extends Service {
    RenderDevice getDevice();
    FrameCounter getFrameCounter();
}
