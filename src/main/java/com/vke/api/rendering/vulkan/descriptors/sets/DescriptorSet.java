package com.vke.api.rendering.vulkan.descriptors.sets;

import com.vke.core.VKEngine;
import com.vke.core.vulkan.device.VulkanRenderDevice;

public class DescriptorSet {

    public final long handle;
    public VulkanRenderDevice device;
    public VKEngine engine;

    public DescriptorSet(long handle, VulkanRenderDevice device, VKEngine engine) {
        this.handle = handle;
        this.device = device;
        this.engine = engine;
    }

    public long getHandle() { return this.handle; }



}
