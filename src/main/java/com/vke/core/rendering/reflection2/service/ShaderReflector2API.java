package com.vke.core.rendering.reflection2.service;

import com.vke.api.rendering.abstraction.renderer.enums.ShaderType;
import com.vke.api.services2.ServiceAPI;
import com.vke.api.services2.ServiceImpl;
import com.vke.core.assets.pipeline.protocols.shader.ShaderPreprocessor;
import com.vke.core.rendering.reflection2.api.ReflectedShader2;
import com.vke.core.services2.Services;
import com.vke.utils.io.Identifier;
import com.vke.utils.iter.helpers.Option;

import java.io.InputStream;

public class ShaderReflector2API extends ServiceAPI implements ShaderReflector2 {
    public ShaderReflector2API(ServiceImpl baseImpl) {
        super(Services.SHADER_REFLECTION, baseImpl);
    }

    private ShaderReflector2 getImpl() {
        return (ShaderReflector2) getImplementation();
    }

    @Override
    public ReflectedShader2 reflect(long id, Identifier ident, InputStream input, ShaderType shaderType, ShaderPreprocessor.ShaderMetadata metadata) {
        return getImpl().reflect(id, ident, input, shaderType, metadata);
    }

    @Override
    public Option<ReflectedShader2> get(long id) {
        return getImpl().get(id);
    }
}
