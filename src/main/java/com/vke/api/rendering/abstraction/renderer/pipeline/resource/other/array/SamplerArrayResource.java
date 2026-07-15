package com.vke.api.rendering.abstraction.renderer.pipeline.resource.other.array;

import com.vke.api.rendering.abstraction.renderer.data.Sampler;
import com.vke.api.rendering.abstraction.renderer.pipeline.resource.ShaderResource;

public interface SamplerArrayResource extends ShaderResource {
    void set(int index, Sampler sampler);
}
