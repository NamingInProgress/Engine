package com.vke.core.rendering.reflection2;

import com.vke.api.rendering.abstraction.renderer.enums.ShaderType;
import com.vke.core.assets.pipeline.protocols.shader.ShaderPreprocessor;
import com.vke.core.rendering.reflection2.api.*;
import com.vke.utils.io.Identifier;

import java.util.List;
import java.util.Map;

public class CoreReflectedShader implements ReflectedShader2 {
    private final ShaderType type;
    private final ShaderPreprocessor.ShaderMetadata meta;
    private final Identifier ident;

    public CoreReflectedShader(Identifier ident, ShaderType shaderType, ShaderPreprocessor.ShaderMetadata metadata, SpirvItem pushConstants, List<SpirvItem> descriptors, List<SpirvItem> vaos) {
        this.type = shaderType;
        this.meta = metadata;
        this.ident = ident;
    }

    @Override
    public PushConstantsResource pushConstants() {
        return null;
    }

    @Override
    public Map<DescriptorCategory, List<DescriptorResource>> descriptors() {
        return Map.of();
    }

    @Override
    public List<VertexAttributeResource> vertexAttributes() {
        return List.of();
    }

    @Override
    public ShaderType getShaderType() {
        return type;
    }

    @Override
    public ShaderPreprocessor.ShaderMetadata getMetadata() {
        return meta;
    }

    @Override
    public Identifier getIdentifier() {
        return ident;
    }

    @Override
    public void free() {

    }
}
