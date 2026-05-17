package com.vke.core.vulkan.shr.service;

import com.vke.api.rendering.abstraction.enums.ShaderType;
import com.vke.api.services2.ServiceAPI;
import com.vke.api.services2.ServiceImpl;
import com.vke.core.assets.pipeline.protocols.shader.ShaderPreprocessor;
import com.vke.core.services2.Services;
import com.vke.core.vulkan.shr.ReflectedShader;
import com.vke.utils.io.Identifier;
import com.vke.utils.iter.helpers.Option;

import java.nio.ByteBuffer;

public class ShaderReflectorAPI extends ServiceAPI implements ShaderReflector {
    public ShaderReflectorAPI(ServiceImpl baseImpl) {
        super(Services.SHADER_REFLECTION, baseImpl);
    }

    private ShaderReflector getImpl() {
        return (ShaderReflector) getImplementation();
    }

    @Override
    public ReflectedShader reflect(long id, Identifier ident, ByteBuffer spirv, ShaderType shaderType, ShaderPreprocessor.ShaderMetadata metadata) {
        return getImpl().reflect(id, ident, spirv, shaderType, metadata);
    }

    @Override
    public Option<ReflectedShader> get(long id) {
        return getImpl().get(id);
    }
}
