package com.vke.core.rendering.vulkan.descriptor;

import com.vke.core.VKEngine;
import com.vke.core.rendering.vulkan.device.LogicalDevice;

import java.util.List;

public class DescriptorPoolCreateInfo {
    public LogicalDevice logicalDevice;
    public int maxSets;
    public List<DescriptorPool.DescriptorTypeCountInfo> descriptorTypeCountInfo;
    public VKEngine engine;
}
