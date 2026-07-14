package com.vke.core.rendering.vulkan.shader.service;

import com.vke.api.rendering.abstraction.renderer.enums.ShaderType;
import com.vke.api.services2.ServiceAPI;
import com.vke.api.services2.ServiceImpl;
import com.vke.core.services2.Services;
import com.vke.utils.io.Identifier;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;

public class ShaderCompilerAPI extends ServiceAPI implements ShaderCompiler {
    public ShaderCompilerAPI(ServiceImpl baseImpl) {
        super(Services.SHADER_COMPILER, baseImpl);
    }

    private ShaderCompiler getImpl() {
        return (ShaderCompiler) getImplementation();
    }

    @Override
    public ByteBuffer compileGlslToSpirV(byte[] shader, ShaderType kind, @NotNull Identifier fileName) throws Exception {
        return getImpl().compileGlslToSpirV(shader, kind, fileName);
    }

    @Override
    public ByteBuffer compileGlslToSpirV(ByteBuffer shader, ShaderType kind, @NotNull Identifier fileName) throws Exception {
        return getImpl().compileGlslToSpirV(shader, kind, fileName);
    }

    @Override
    public ByteBuffer compileGlslToSpirV(byte[] shader, int kind, @NotNull Identifier fileName) throws Exception {
        return getImpl().compileGlslToSpirV(shader, kind, fileName);
    }

    @Override
    public ByteBuffer compileGlslToSpirV(ByteBuffer source, int kind, @NotNull Identifier fileName) throws Exception {
        return getImpl().compileGlslToSpirV(source, kind, fileName);
    }
}
