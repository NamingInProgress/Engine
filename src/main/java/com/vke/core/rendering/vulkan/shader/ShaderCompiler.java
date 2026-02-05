package com.vke.core.rendering.vulkan.shader;

import com.vke.api.services.Service;
import com.vke.core.memory.AutoHeapAllocator;
import com.vke.core.services.Services;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.util.shaderc.Shaderc;

import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.List;

public class ShaderCompiler extends Service {
    private long compiler;

    public ShaderCompiler() {
        super(Services.SHADER_COMPILER);
        compiler = Shaderc.shaderc_compiler_initialize();
    }

    public ByteBuffer compileGlslToSpirV(byte[] shader, Shader.Type kind, @Nullable String fileName) throws Exception {
        return this.compileGlslToSpirV(shader, kind.getShadercHandle(), fileName);
    }

    public ByteBuffer compileGlslToSpirV(byte[] shader, int kind, @Nullable String fileName) throws Exception {
        AutoHeapAllocator alloc = new AutoHeapAllocator();
        ByteBuffer source = alloc.bytes(shader).getHeapObject();
        long options = Shaderc.shaderc_compile_options_initialize();
        long result = Shaderc.shaderc_compile_into_spv(compiler, source, kind, bytes(alloc, fileName), bytes(alloc, "main"), options);
        Shaderc.shaderc_compile_options_release(options);
        int status = Shaderc.shaderc_result_get_compilation_status(result);
        alloc.close();
        if (status != Shaderc.shaderc_compilation_status_success) {
            String error = Shaderc.shaderc_result_get_error_message(result);
            throw new Exception(error);
        }
        ByteBuffer buf = Shaderc.shaderc_result_get_bytes(result);
        long length = Shaderc.shaderc_result_get_length(result);
        ByteBuffer spirv = ByteBuffer.allocateDirect((int) length);
        spirv.put(buf);
        spirv.flip();

        Shaderc.shaderc_result_release(result);
        return spirv;
    }

    private ByteBuffer bytes(AutoHeapAllocator alloc, @Nullable String s) {
        if (s == null) return bytes(alloc, "not specified\0");
        return alloc.utf8(s).getHeapObject();
    }

    @Override
    public void free() {
        Shaderc.shaderc_compiler_release(compiler);
    }

    @Override
    public List<String> dependencies() {
        return Collections.emptyList();
    }

}
