package com.vke.core.rendering.vulkan.descriptor;

import com.vke.core.VKEngine;
import com.vke.core.rendering.vulkan.VulkanSetup;
import com.vke.core.rendering.vulkan.device.LogicalDevice;
import com.vke.core.vulkan.device.VulkanRenderDevice;

import java.util.List;

public class DescriptorPoolCreateInfo {
    public VulkanRenderDevice device;
    public int maxSets;
    public List<DescriptorPool.DescriptorTypeCountInfo> descriptorTypeCountInfo;
    public VKEngine engine;
}
