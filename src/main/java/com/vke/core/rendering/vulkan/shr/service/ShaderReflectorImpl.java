package com.vke.core.rendering.vulkan.shr.service;

import com.vke.api.logger.Logger;
import com.vke.api.rendering.abstraction.renderer.enums.ShaderType;
import com.vke.api.services2.ServiceImpl;
import com.vke.core.VKEngine;
import com.vke.core.assets.pipeline.protocols.shader.ShaderPreprocessor;
import com.vke.core.logger.LoggerFactory;
import com.vke.core.services2.Services;
import com.vke.core.rendering.vulkan.shr.ReflectedShader;
import com.vke.utils.io.Disposable;
import com.vke.utils.io.Identifier;
import com.vke.utils.iter.helpers.Option;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.util.spvc.Spvc;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;

public class ShaderReflectorImpl extends ServiceImpl implements ShaderReflector {

    private static final Logger logger = LoggerFactory.get("Shader Reflection");

    private long spvcContext;

    private final HashMap<Long, ReflectedShader> CACHE = new HashMap<>();

    public ShaderReflectorImpl(VKEngine engine) {
        super(Services.SHADER_REFLECTION, engine);
    }

    @Override
    protected void onInitialize() {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            PointerBuffer pContext = stack.mallocPointer(1);

            if (Spvc.spvc_context_create(pContext) != Spvc.SPVC_SUCCESS) {
                engine.throwException(new IllegalStateException("Failed to create SPIRV-Cross Context!"), "ShaderReflector");
            }

            this.spvcContext = pContext.get(0);

            Spvc.spvc_context_set_error_callback(spvcContext,
                    (userData, error) -> logger.error("SPIRV-Cross error: " + MemoryUtil.memUTF8(error)),
                    0);
        }
    }

    @Override
    public ReflectedShader reflect(long id, Identifier ident, ByteBuffer spirv, ShaderType shaderType, ShaderPreprocessor.ShaderMetadata metadata) {
        if (CACHE.containsKey(id)) return CACHE.get(id);
        ReflectedShader s = new ReflectedShader(ident, spvcContext, spirv, shaderType, metadata);
        CACHE.put(id, s);
        return s;
    }

    @Override
    public Option<ReflectedShader> get(long id) {
        return Option.useIfNotNull(CACHE.get(id));
    }

    @Override
    public List<String> dependencies() {
        return List.of(Services.SHADER_COMPILER);
    }

    @Override
    public void free() {
        CACHE.values().forEach(Disposable::free);
        Spvc.spvc_context_destroy(spvcContext);
    }
}
