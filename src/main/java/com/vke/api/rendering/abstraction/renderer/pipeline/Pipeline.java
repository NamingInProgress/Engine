package com.vke.api.rendering.abstraction.renderer.pipeline;

import com.vke.api.rendering.abstraction.renderer.pipeline.resource.ShaderResource;
import com.vke.utils.io.Disposable;

public interface Pipeline extends Disposable {

    PipelineLayout layout();
    <T extends ShaderResource> T resource(String name);

}
