package com.vke.core.rendering.reflection2.service;

import com.vke.api.services2.ServiceAPI;
import com.vke.api.services2.ServiceImpl;
import com.vke.core.rendering.reflection2.api.ReflectedShader2;

import java.io.InputStream;

public class ShaderReflector2API extends ServiceAPI implements ShaderReflector2 {
    public ShaderReflector2API(ServiceImpl baseImpl) {
        super(baseImpl.getId(), baseImpl);
    }

    private ShaderReflector2 getImpl() {
        return (ShaderReflector2) getImplementation();
    }

    @Override
    public ReflectedShader2 reflect(InputStream input) {
        return getImpl().reflect(input);
    }
}
