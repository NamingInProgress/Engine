package com.vke.api.rendering.abstraction.renderer.pipeline.resource.other;

import com.vke.api.rendering.abstraction.renderer.data.Sampler;
import com.vke.api.rendering.abstraction.renderer.pipeline.resource.ShaderResource;

public interface SamplerResource extends ShaderResource {
    void set(Sampler sampler);
}
