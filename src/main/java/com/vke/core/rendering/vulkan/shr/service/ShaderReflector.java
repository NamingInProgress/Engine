package com.vke.core.rendering.vulkan.shr.service;

import com.vke.api.rendering.abstraction.renderer.enums.ShaderType;
import com.vke.api.services2.Service;
import com.vke.core.assets.pipeline.protocols.shader.ShaderPreprocessor;
import com.vke.core.rendering.vulkan.shr.ReflectedShader;
import com.vke.utils.io.Identifier;
import com.vke.utils.iter.helpers.Option;

import java.nio.ByteBuffer;

public interface ShaderReflector extends Service {
    ReflectedShader reflect(long id, Identifier ident, ByteBuffer spirv, ShaderType shaderType, ShaderPreprocessor.ShaderMetadata metadata);

    Option<ReflectedShader> get(long id);
}
