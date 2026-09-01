package com.vke.api.rendering.abstraction.draw;

import com.vke.api.rendering.abstraction.renderer.data.RenderingEncoder;

public interface SelfPuttable {
    void putSelf(RenderingEncoder buf);
}
