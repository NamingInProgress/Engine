package com.vke.core.rendering.vulkan.shader.service;

import com.vke.api.rendering.abstraction.enums.ShaderType;
import com.vke.api.app.Version;
import com.vke.api.services2.ServiceImpl;
import com.vke.api.vkz.ArchiveType;
import com.vke.api.vkz.VkzArchive;
import com.vke.api.vkz.VkzEditor;
import com.vke.core.VKEngine;
import com.vke.core.memory.AutoHeapAllocator;
import com.vke.core.services2.Services;
import com.vke.core.vkz.service.Vkz;
import com.vke.utils.io.FileUtils;
import com.vke.utils.io.Identifier;
import com.vke.utils.Utils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.util.shaderc.Shaderc;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;

public class ShaderCompilerImpl extends ServiceImpl implements ShaderCompiler {
    private static final HashMap<String, ByteBuffer> CACHE = new HashMap<>();

    private long compiler;
    private AutoHeapAllocator alloc;
    private VKEngine engine;
    private Vkz vkz;
    private boolean hasBeenFreed;

    public ShaderCompilerImpl(VKEngine engine) {
        super(Services.SHADER_COMPILER, engine);
        this.engine = engine;
    }

    @Override
    protected void onInitialize() {
        this.compiler = Shaderc.shaderc_compiler_initialize();
        //System.out.println("making compiler: " + Long.toHexString(compiler));
        this.alloc = new AutoHeapAllocator();
        this.vkz = engine.service(Services.VKZ);

        loadCacheFromArchive();
    }

    @Override
    public ByteBuffer compileGlslToSpirV(byte[] shader, ShaderType kind, @NotNull Identifier fileName) throws Exception {
        return this.compileGlslToSpirV(alloc.bytes(shader).getHeapObject(), kind.getShadercHandle(), fileName);
    }

    @Override
    public ByteBuffer compileGlslToSpirV(ByteBuffer shader, ShaderType kind, @NotNull Identifier fileName) throws Exception {
        return this.compileGlslToSpirV(shader, kind.getShadercHandle(), fileName);
    }

    @Override
    public ByteBuffer compileGlslToSpirV(byte[] shader, int kind, @NotNull Identifier fileName) throws Exception {
        return this.compileGlslToSpirV(alloc.bytes(shader).getHeapObject(), kind, fileName);
    }

    @Override
    public ByteBuffer compileGlslToSpirV(ByteBuffer source, int kind, @NotNull Identifier fileName) throws Exception {
        if (CACHE.containsKey(fileName.toSpecialVkzFormatCuzItsBad())) return CACHE.get(fileName.toSpecialVkzFormatCuzItsBad());

        long options = Shaderc.shaderc_compile_options_initialize();

        Shaderc.shaderc_compile_options_set_target_env(options, Shaderc.shaderc_target_env_vulkan, Shaderc.shaderc_env_version_vulkan_1_4);
        Shaderc.shaderc_compile_options_set_target_spirv(options, Shaderc.shaderc_spirv_version_1_4);
        Shaderc.shaderc_compile_options_set_source_language(
                options,
                Shaderc.shaderc_source_language_glsl
        );
        long result = Shaderc.shaderc_compile_into_spv(compiler, source, kind, bytes(alloc, fileName.toSpecialVkzFormatCuzItsBad()), bytes(alloc, "main"), options);
        Shaderc.shaderc_compile_options_release(options);
        int status = Shaderc.shaderc_result_get_compilation_status(result);
        if (status != Shaderc.shaderc_compilation_status_success) {
            String error = Shaderc.shaderc_result_get_error_message(result);
            throw new Exception(error);
        }
        ByteBuffer buf = Shaderc.shaderc_result_get_bytes(result);
        long length = Shaderc.shaderc_result_get_length(result);
        ByteBuffer spirv = alloc.allocByteBuffer((int) length).getHeapObject();
        spirv.put(buf);
        spirv.flip();

        Shaderc.shaderc_result_release(result);
        CACHE.put(fileName.toSpecialVkzFormatCuzItsBad(), spirv);
        return spirv;
    }

    private ByteBuffer bytes(AutoHeapAllocator alloc, @Nullable String s) {
        if (s == null) return bytes(alloc, "not specified\0");
        return alloc.utf8(s).getHeapObject();
    }

    public void loadCacheFromArchive() {
        if (engine.isDebugMode()) return;

        try {
            Path cacheFolder = FileUtils.getCacheFolder(engine.getApp().getName(), false);
            Path archivePath = Path.of(cacheFolder + "/shaders.vkz");
            if (!archivePath.toFile().exists()) return;

            VkzArchive archive = vkz.open(new FileInputStream(archivePath.toFile()), ArchiveType.InflateAll);

            Version ver = Version.fromString(Utils.readStringFromInputStream(archive.file("CACHE_VERSION.txt").getInputStream()));
            if (!ver.equals(engine.getAppVersion())) {
                removeArchive();
                return;
            }

            archive.iterateFiles().forEach((f) -> {
                try {
                    CACHE.put(f.getName(), alloc.bytes(f.getInputStream().readAllBytes()).getHeapObject());
                } catch (IOException e) {
                    engine.throwException(e, "ShaderCompiler@CacheLoad@" + f.getName());
                }
            });
        } catch (Exception e) {
            engine.getLogger().error("Exception at ShaderCompiler@CacheLoad: %s", e);
        }
    }

    public void dumpCacheToArchive() {
        try {
            VkzArchive archive = vkz.createNew();
            Path cacheFolder = FileUtils.getCacheFolder(engine.getApp().getName());
            Path archivePath = Path.of(cacheFolder + "/shaders.vkz");
            if (!archivePath.toFile().exists()) Files.createFile(archivePath);

            VkzEditor version = archive.root().createFile("CACHE_VERSION.txt").edit();
            version.write(engine.getAppVersion().toString());
            version.commit();

            CACHE.forEach((name, buf) -> {
                VkzEditor editor = archive.root().createFile(name).edit();
                editor.write(Utils.acquireByteArrayFromBuffer(buf));
                editor.commit();
            });

            archive.writeOut(new FileOutputStream(cacheFolder + "/shaders.vkz"));
        } catch (Exception e) {
            engine.throwException(e, "ShaderCompiler@CacheDump");
        }
    }

    public void removeArchive() {
        try {
            Path cacheFolder = FileUtils.getCacheFolder(engine.getApp().getName());
            Path archivePath = Path.of(cacheFolder + "/shaders.vkz");

            Files.deleteIfExists(archivePath);
        } catch (Exception e) {
            engine.throwException(e, "ShaderCompiler@RemoveArchive");
        }
    }

    @Override
    public void free() {
        if (!hasBeenFreed) {
            dumpCacheToArchive();
            alloc.close();
            //we, the nip team, dont care about your ram :) if you cant afford more ram,
            // you sadly cant be using this engine -> go use unity or something.
            //System.out.println("bla bla bla shaderc free compiler ahgh handle: " + Long.toHexString(compiler));
            //Shaderc.shaderc_compiler_release(compiler);
            hasBeenFreed = true;
        }
    }

    @Override
    public List<String> dependencies() {
        return List.of(Services.VKZ);
    }
}
