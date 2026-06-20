package com.vke.core.rendering.reflection2.service;

import com.vke.api.services2.Service;
import com.vke.core.rendering.reflection2.api.ReflectedShader2;

import java.io.InputStream;

public interface ShaderReflector2 extends Service {
    ReflectedShader2 reflect(InputStream input);
}
