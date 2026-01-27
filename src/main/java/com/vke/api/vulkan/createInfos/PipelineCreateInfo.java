package com.vke.api.vulkan.createInfos;

import com.vke.core.VKEngine;
import com.vke.core.rendering.vulkan.device.LogicalDevice;
import com.vke.core.rendering.vulkan.swapchain.SwapChain;
import com.vke.utils.Identifier;

public class PipelineCreateInfo {

    public LogicalDevice device;
    public VKEngine engine;
    public SwapChain swapChain;
    public Identifier name;

}
