package com.vke.core.rendering.vulkan.descriptor;

import com.vke.core.rendering.vulkan.device.LogicalDevice;
import org.lwjgl.vulkan.VkDescriptorSetLayoutBinding;

import java.util.List;

public class DescriptorSetLayoutCreateInfo {
    public LogicalDevice device;
    public List<VkDescriptorSetLayoutBinding> bindings;
}
