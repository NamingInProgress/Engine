package com.vke.api.registry.registries;

import com.vke.api.abstraction.RenderDevice;
import com.vke.api.vulkan.pipeline.RenderPipeline;
import com.vke.core.VKEngine;
import com.vke.core.rendering.vulkan.VulkanSetup;
import com.vke.core.vulkan.device.VulkanRenderDevice;
import com.vke.utils.Identifier;

public class PipelinesRegistry extends VKERegistry.ID<RenderPipeline> {

    public PipelinesRegistry(Identifier registryName) {
        super(registryName);
    }

    public void makeVkPipelines(VKEngine engine, VulkanRenderDevice device) {
        this.values.forEach((_, pipeline) -> {
            try {
                pipeline.setupGraphicsPipeline(engine, device);
            } catch (Exception e) {
                engine.throwException(e, "Pipelines Registry");
            }
        });
    }

    public void freeVkPipelines() {
        this.values.forEach((_, pipeline) -> {
            pipeline.free();
        });
    }

}
