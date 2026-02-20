package com.vke.api.app;

import com.vke.core.VKEngine;
import com.vke.core.vulkan.VulkanRenderer;
import com.vke.core.window.Window;
import com.vke.utils.Disposable;
import com.vke.utils.Identifier;

import java.io.InputStream;

public abstract class App implements Disposable {
    public abstract void onInit(VKEngine engine);
    public abstract void onDraw(Window window, VulkanRenderer.FrameData fd);
    public abstract String getName();
}
