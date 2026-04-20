package com.vke.core.vulkan.descriptor.data;

import com.vke.core.Context;
import com.vke.core.vulkan.device.VulkanRenderDevice;

public class ShaderDataCreateInfo {

    // General
    public final Context context;
    public VulkanRenderDevice device;
    public int framesInFlight;

    // Buffers
    public long minUboAlign;
    public long frameDataBufferSize;

    // Textures
    public int maxTexturesCount;

    public ShaderDataCreateInfo(Context context) {
        this.context = context;
    }

    public ShaderDataCreateInfo device(VulkanRenderDevice device) {
        this.device = device;
        return this;
    }

    public ShaderDataCreateInfo framesInFlight(int framesInFlight) {
        this.framesInFlight = framesInFlight;
        return this;
    }

    public ShaderDataCreateInfo minUboAlign(long minUboAlign) {
        this.minUboAlign = minUboAlign;
        return this;
    }

    public ShaderDataCreateInfo frameDataBufferSize(long frameDataBufferSize) {
        this.frameDataBufferSize = frameDataBufferSize;
        return this;
    }

    public ShaderDataCreateInfo maxTexturesCount(int maxTexturesCount) {
        this.maxTexturesCount = maxTexturesCount;
        return this;
    }
}
