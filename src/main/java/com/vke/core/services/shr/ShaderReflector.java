package com.vke.core.services.shr;

import com.vke.api.logger.Logger;
import com.vke.api.services.Service;
import com.vke.core.VKEngine;
import com.vke.core.logger.LoggerFactory;
import com.vke.core.services.Services;
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

public class ShaderReflector extends Service {

    private static final Logger logger = LoggerFactory.get("Shader Reflection");

    private final long spvcContext;

    private final HashMap<Identifier, ReflectedShader> CACHE = new HashMap<>();

    public ShaderReflector(VKEngine engine) {
        super(Services.SHADER_REFLECTION);

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

    public ReflectedShader reflect(Identifier id, ByteBuffer spirv) {
        if (CACHE.containsKey(id)) return CACHE.get(id);
        ReflectedShader s = new ReflectedShader(spvcContext, spirv);
        CACHE.put(id, s);
        return s;
    }

    public Option<ReflectedShader> get(Identifier id) {
        return Option.useIfNotNull(CACHE.get(id));
    }

    @Override
    protected List<String> dependencies() {
        return List.of(Services.SHADER_COMPILER);
    }

    @Override
    public void free() {
        CACHE.values().forEach(Disposable::free);
        Spvc.spvc_context_destroy(spvcContext);
    }

}
