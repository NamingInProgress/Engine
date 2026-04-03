package com.vke.core.assets.handles.rendering.shader;

import com.vke.api.rendering.abstraction.RenderDevice;
import com.vke.api.rendering.abstraction.enums.ShaderType;
import com.vke.api.rendering.abstraction.shader.Shader;
import com.vke.core.VKEngine;
import com.vke.core.assets.handles.rendering.RenderingAssetHandle;
import com.vke.utils.io.Identifier;

import java.io.IOException;

public class ShaderAssetHandle extends RenderingAssetHandle<Shader> {
    private final Identifier identifier;
    private final ShaderType type;

    public ShaderAssetHandle(Identifier identifier, ShaderType type) {
        this.identifier = identifier;
        this.type = type;
    }

    @Override
    public Type getType() {
        return Type.Shader;
    }

    @Override
    public Shader acquire(VKEngine engine, RenderDevice device) throws IOException {
        return device.createShader(identifier, type);
    }

    @Override
    public void free() {
        if (isAvailable()) {
            get().free();
        }
        setCache(null);
    }
}
