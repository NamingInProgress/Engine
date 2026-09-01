package com.vke.core.rendering.reflection2.service;

import com.vke.api.rendering.abstraction.renderer.enums.ShaderType;
import com.vke.api.services2.Service;
import com.vke.core.Identifier;
import com.vke.core.assets.pipeline.protocols.shader.ShaderPreprocessor;
import com.vke.core.rendering.reflection2.api.ReflectedShader2;
import com.vke.utils.iter.helpers.Option;

import java.io.InputStream;

public interface ShaderReflector2 extends Service {
    ReflectedShader2 reflect(long id, Identifier ident, InputStream input, ShaderType shaderType, ShaderPreprocessor.ShaderMetadata metadata);

    Option<ReflectedShader2> get(long id);
}
