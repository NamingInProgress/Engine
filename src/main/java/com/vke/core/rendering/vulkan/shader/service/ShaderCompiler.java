package com.vke.core.rendering.vulkan.shader.service;

import com.vke.api.rendering.abstraction.renderer.enums.ShaderType;
import com.vke.api.services2.Service;
import com.vke.core.FileIdentifier;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;

public interface ShaderCompiler extends Service {
    ByteBuffer compileGlslToSpirV(byte[] shader, ShaderType kind, @NotNull FileIdentifier fileName) throws Exception;
    ByteBuffer compileGlslToSpirV(ByteBuffer shader, ShaderType kind, @NotNull FileIdentifier fileName) throws Exception;
    ByteBuffer compileGlslToSpirV(byte[] shader, int kind, @NotNull FileIdentifier fileName) throws Exception;
    ByteBuffer compileGlslToSpirV(ByteBuffer source, int kind, @NotNull FileIdentifier fileName) throws Exception;
}
