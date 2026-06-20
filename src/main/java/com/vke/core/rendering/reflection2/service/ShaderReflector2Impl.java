package com.vke.core.rendering.reflection2.service;

import com.vke.api.services2.ServiceImpl;
import com.vke.core.VKEngine;
import com.vke.core.rendering.reflection2.api.ReflectedShader2;
import com.vke.core.services2.Services;

import java.io.InputStream;
import java.util.List;

public class ShaderReflector2Impl extends ServiceImpl implements ShaderReflector2 {
    public ShaderReflector2Impl(VKEngine engine) {
        super(Services.SHADER_REFLECTION2, engine);
    }

    @Override
    protected void onInitialize() {

    }

    @Override
    public List<String> dependencies() {
        return List.of();
    }

    @Override
    public ReflectedShader2 reflect(InputStream input) {

        return null;
    }

    @Override
    public void free() {

    }
}
