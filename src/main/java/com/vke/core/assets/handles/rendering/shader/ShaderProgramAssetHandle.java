package com.vke.core.assets.handles.rendering.shader;

import com.vke.api.abstraction.RenderDevice;
import com.vke.api.abstraction.descriptors.ShaderType;
import com.vke.api.vulkan.shaders.ShaderProgram;
import com.vke.core.VKEngine;
import com.vke.core.assets.handles.rendering.RenderingAssetHandle;
import com.vke.utils.Identifier;

import java.io.IOException;
import java.util.Map;

public class ShaderProgramAssetHandle extends RenderingAssetHandle<ShaderProgram> {
    private final Map<ShaderType, Identifier> sources;

    public ShaderProgramAssetHandle(Map<ShaderType, Identifier> sources) {
        this.sources = sources;
    }

    @Override
    protected ShaderProgram acquire(VKEngine engine, RenderDevice renderDevice) throws IOException {
        return renderDevice.createShaders(sources);
    }

    @Override
    public Type getType() {
        return Type.ShaderProgram;
    }

    @Override
    public void free() {
        setCache(null);
    }
}
