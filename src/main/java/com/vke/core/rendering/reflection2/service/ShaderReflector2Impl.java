package com.vke.core.rendering.reflection2.service;

import com.carrotsearch.hppc.LongObjectHashMap;
import com.carrotsearch.hppc.procedures.ObjectProcedure;
import com.vke.api.logger.Logger;
import com.vke.api.rendering.abstraction.renderer.enums.ShaderType;
import com.vke.api.services2.ServiceImpl;
import com.vke.core.Identifier;
import com.vke.core.VKEngine;
import com.vke.core.assets.pipeline.protocols.shader.ShaderPreprocessor;
import com.vke.core.logger.LoggerFactory;
import com.vke.core.rendering.reflection2.CoreReflector;
import com.vke.core.rendering.reflection2.api.ReflectedShader2;
import com.vke.core.services2.Services;
import com.vke.utils.io.Disposable;
import com.vke.utils.iter.helpers.Option;

import java.io.InputStream;
import java.util.List;

public class ShaderReflector2Impl extends ServiceImpl implements ShaderReflector2 {
    private static final Logger logger = LoggerFactory.get("Shader Reflection 2");

    private final LongObjectHashMap<ReflectedShader2> CACHE = new LongObjectHashMap<>();

    public ShaderReflector2Impl(VKEngine engine) {
        super(Services.SHADER_REFLECTION2, engine);
    }

    @Override
    protected void onInitialize() {}

    @Override
    public ReflectedShader2 reflect(long id, Identifier ident, InputStream input, ShaderType shaderType, ShaderPreprocessor.ShaderMetadata metadata) {
        if (CACHE.containsKey(id)) return CACHE.get(id);
        CoreReflector reflector = new CoreReflector(input);
        ReflectedShader2 s = reflector.reflect(ident, shaderType, metadata);
        CACHE.put(id, s);
        return s;
    }

    @Override
    public Option<ReflectedShader2> get(long id) {
        return Option.useIfNotNull(CACHE.get(id));
    }

    @Override
    public List<String> dependencies() {
        return List.of(Services.SHADER_COMPILER);
    }

    @Override
    public void free() {
        CACHE.values().forEach((ObjectProcedure<? super ReflectedShader2>) Disposable::free);
    }
}
