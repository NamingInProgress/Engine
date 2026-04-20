package com.vke.core.vulkan.shr.service;

import com.vke.api.rendering.abstraction.enums.ShaderType;
import com.vke.api.services2.Service;
import com.vke.core.vulkan.shr.ReflectedShader;
import com.vke.utils.iter.helpers.Option;

import java.nio.ByteBuffer;

public interface ShaderReflector extends Service {
    ReflectedShader reflect(long id, ByteBuffer spirv, ShaderType shaderType);

    Option<ReflectedShader> get(long id);
}
