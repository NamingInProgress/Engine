package com.vke.core.rendering.vulkan.createInfos;

import com.vke.core.VKEngine;
import com.vke.core.rendering.vulkan.VulkanSetup;
import com.vke.core.rendering.vulkan.device.LogicalDevice;
import com.vke.core.rendering.vulkan.swapchain.SwapChain;
import com.vke.core.vulkan.device.VulkanRenderDevice;
import com.vke.utils.Identifier;

public class PipelineCreateInfo {

    public VulkanRenderDevice device;
    public VKEngine engine;
    public Identifier name;

}
