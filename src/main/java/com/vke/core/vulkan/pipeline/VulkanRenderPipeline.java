package com.vke.core.vulkan.pipeline;

import com.vke.api.rendering.abstraction.pipeline.GraphicsPipeline;
import com.vke.api.rendering.abstraction.pipeline.PipelineLayout;
import com.vke.core.VKEngine;
import com.vke.core.vulkan.device.VulkanRenderDevice;

public class VulkanRenderPipeline implements GraphicsPipeline {

    private final VKEngine engine;
    private final VulkanRenderDevice device;

    public VulkanRenderPipeline(VKEngine engine, VulkanRenderDevice device) {
        this.engine = engine;
        this.device = device;
    }

    @Override
    public PipelineLayout layout() {
        return null;
    }

    @Override
    public void free() {

    }

}
