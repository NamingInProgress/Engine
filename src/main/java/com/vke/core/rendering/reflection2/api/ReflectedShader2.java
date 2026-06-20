package com.vke.core.rendering.reflection2.api;

import com.vke.utils.iter.Iter;

public interface ReflectedShader2 {
    Iter<ShaderElement> uniforms();
    Iter<ShaderElement> buffers();
}
