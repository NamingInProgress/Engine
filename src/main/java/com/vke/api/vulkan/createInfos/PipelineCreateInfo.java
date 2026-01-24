package com.vke.api.vulkan.createInfos;

import com.vke.core.VKEngine;
import com.vke.core.rendering.vulkan.device.LogicalDevice;
import com.vke.core.rendering.vulkan.shader.ShaderProgram;
import com.vke.core.rendering.vulkan.swapchain.SwapChain;
import org.lwjgl.vulkan.VkPipelineShaderStageCreateInfo;

public class PipelineCreateInfo {

    public LogicalDevice device;
    public ShaderProgram shader;
    public VKEngine engine;
    public SwapChain swapChain;
    public VkPipelineShaderStageCreateInfo[] shaderModuleCreateInfos;

}
