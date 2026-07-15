package com.vke.api.rendering.abstraction.renderer.pipeline;

import com.vke.api.rendering.abstraction.renderer.pipeline.resource.ShaderResource;
import com.vke.utils.io.Disposable;

public interface PipelineLayout extends Disposable {

    <T extends ShaderResource> T resource(String name);
    int pushConstantSize();
    int descriptorCount();

}
