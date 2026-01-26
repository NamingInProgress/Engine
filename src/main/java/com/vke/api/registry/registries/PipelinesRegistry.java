package com.vke.api.registry.registries;

import com.vke.api.vulkan.pipeline.RenderPipeline;
import com.vke.core.VKEngine;
import com.vke.core.rendering.vulkan.VulkanSetup;
import com.vke.utils.Identifier;

public class PipelinesRegistry extends VKERegistry.ID<RenderPipeline> {

    public PipelinesRegistry(Identifier registryName) {
        super(registryName);
    }

    public void makeVkPipelines(VKEngine engine, VulkanSetup vkSetup) {
        this.values.forEach((_, pipeline) -> {
            try {
                pipeline.setupGraphicsPipeline(engine, vkSetup);
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
