package com.vke.core.rendering.vulkan.shader;

import com.vke.core.memory.AutoHeapAllocator;
import com.vke.utils.Disposable;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.util.shaderc.Shaderc;

import java.nio.ByteBuffer;

public class ShaderCompiler implements Disposable {
    private long compiler;

    public ShaderCompiler() {
        compiler = Shaderc.shaderc_compiler_initialize();
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
}
