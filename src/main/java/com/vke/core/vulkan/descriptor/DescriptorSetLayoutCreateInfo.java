package com.vke.core.vulkan.descriptor;

import com.vke.core.vulkan.device.LogicalDevice;
import org.lwjgl.vulkan.VkDescriptorSetLayoutBinding;

import java.util.List;

public class DescriptorSetLayoutCreateInfo {
    public LogicalDevice device;
    public List<VkDescriptorSetLayoutBinding> bindings;
}
