package com.vke.core;

import com.vke.api.app.Version;
import com.vke.core.services2.Services;
import com.vke.core.vulkan.createInfos.VulkanCreateInfo;
import com.vke.api.window.WindowCreateInfo;

public class EngineCreateInfo {
    public WindowCreateInfo windowCreateInfo;
    public VulkanCreateInfo vulkanCreateInfo;
    public String applicationName;
    public String applicationNamespace;
    public Version applicationVersion;
    public boolean releaseMode;
    public boolean vsync;
    public RendererType rendererType;

    public final String engine = "VkEngine";
    public final Version engineVersion = Version.V1_0_0;
    public int fps = 60;

    public EngineCreateInfo(String applicationName, String namespace) {
        this.windowCreateInfo = new WindowCreateInfo();
        this.vulkanCreateInfo = new VulkanCreateInfo();
        this.applicationName = applicationName;
        this.applicationVersion = Version.V1_0_0;
        this.releaseMode = true;
        this.applicationNamespace = namespace;
        this.rendererType = RendererType.Vulkan;
    }

    public enum RendererType {
        Vulkan(Services.VULKAN_RENDERER);

        public final String serviceName;

        RendererType(String type) {
            this.serviceName = type;
        }
    }
}
