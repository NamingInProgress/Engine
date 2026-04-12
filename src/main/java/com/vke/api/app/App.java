package com.vke.api.app;

import com.vke.core.VKEngine;
import com.vke.core.rendering.draw.DrawContext;
import com.vke.core.vulkan.VulkanRenderer;
import com.vke.core.window.Window;
import com.vke.utils.io.Disposable;

public abstract class App implements Disposable {
    public abstract void onInit(VKEngine engine);
    public abstract void onDraw(DrawContext ctx);
    public abstract String getName();
}
