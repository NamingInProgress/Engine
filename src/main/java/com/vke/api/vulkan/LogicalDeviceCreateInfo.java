package com.vke.api.vulkan;

import com.vke.core.EngineCreateInfo;
import com.vke.core.rendering.vulkan.PhysicalDevice;
import org.lwjgl.vulkan.VkPhysicalDevice;

public class LogicalDeviceCreateInfo {

    public EngineCreateInfo engineCreateInfo;
    public PhysicalDevice physicalDeviceWrapper;
    public VkPhysicalDevice physicalDevice;
    public long surfaceHandle;

    public LogicalDeviceCreateInfo(EngineCreateInfo engineCreateInfo, PhysicalDevice physicalDevice) {
        this.engineCreateInfo = engineCreateInfo;
        this.physicalDeviceWrapper = physicalDevice;
        this.physicalDevice = physicalDevice.getDevice();
    }

    public LogicalDeviceCreateInfo() {}

}
