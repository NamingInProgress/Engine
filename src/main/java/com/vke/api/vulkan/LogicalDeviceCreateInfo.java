package com.vke.api.vulkan;

import com.vke.core.EngineCreateInfo;
import org.lwjgl.vulkan.VkPhysicalDevice;

public class LogicalDeviceCreateInfo {

    public EngineCreateInfo engineCreateInfo;
    public VkPhysicalDevice physicalDevice;
    public int[] queueIndices;

    public LogicalDeviceCreateInfo(EngineCreateInfo engineCreateInfo, VkPhysicalDevice physicalDevice, int[] queueIndices) {
        this.engineCreateInfo = engineCreateInfo;
        this.physicalDevice = physicalDevice;
        this.queueIndices = queueIndices;
    }

    public LogicalDeviceCreateInfo() {}

}
