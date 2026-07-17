package com.vke.api.rendering.abstraction.renderer;

import com.vke.api.rendering.abstraction.renderer.data.Sampler;
import com.vke.api.rendering.abstraction.renderer.data.StaticMesh;
import com.vke.core.mesh.Mesh;
import com.vke.utils.io.Disposable;

public interface RenderResourceManager extends Disposable {
    StaticMesh uploadStaticMesh(Mesh<?> mesh);
    Sampler samplerFromStringOrDefault(String samplerName);
    void registerSampler(String name, Sampler sampler);
}
