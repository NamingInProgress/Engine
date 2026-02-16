package com.vke.core.vulkan.descriptor;

import com.vke.core.VKEngine;
import com.vke.core.vulkan.device.VulkanRenderDevice;

import java.util.List;

public class DescriptorPoolCreateInfo {
    public VulkanRenderDevice device;
    public int maxSets;
    public List<DescriptorPool.DescriptorTypeCountInfo> descriptorTypeCountInfo;
    public VKEngine engine;
}
