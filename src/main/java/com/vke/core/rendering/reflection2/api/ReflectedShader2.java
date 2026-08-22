package com.vke.core.rendering.reflection2.api;

import com.vke.api.rendering.abstraction.renderer.enums.ShaderType;
import com.vke.core.assets.pipeline.protocols.shader.ShaderPreprocessor;
import com.vke.utils.io.Disposable;
import com.vke.utils.io.Identifier;

import java.util.List;
import java.util.Map;

public interface ReflectedShader2 extends Disposable {
    PushConstantsResource pushConstants();
    Map<DescriptorCategory, List<DescriptorResource>> descriptors();
    List<VertexAttributeResource> vertexAttributes();

    ShaderType getShaderType();
    ShaderPreprocessor.ShaderMetadata getMetadata();
    Identifier getIdentifier();
}
