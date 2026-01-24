package com.vke.core;

import com.vke.api.game.Version;
import com.vke.api.vulkan.createInfos.VulkanCreateInfo;
import com.vke.api.window.WindowCreateInfo;

public class EngineCreateInfo {

    public WindowCreateInfo windowCreateInfo;
    public VulkanCreateInfo vulkanCreateInfo;
    public String applicationName;
    public Version applicationVersion;
    public boolean releaseMode;
    public boolean vsync;

    public final String engine = "VkEngine";
    public final Version engineVersion = Version.V1_0_0;

    public EngineCreateInfo() {
        windowCreateInfo = new WindowCreateInfo();
        vulkanCreateInfo = new VulkanCreateInfo();
        applicationName = "HelloApplication";
        applicationVersion = Version.V1_0_0;
        releaseMode = true;
    }
}
