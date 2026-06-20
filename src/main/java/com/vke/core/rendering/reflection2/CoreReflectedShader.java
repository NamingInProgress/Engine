package com.vke.core.rendering.reflection2;

import com.vke.core.rendering.reflection2.api.ReflectedShader2;
import com.vke.core.rendering.reflection2.api.ShaderElement;
import com.vke.utils.iter.Iter;

public class CoreReflectedShader implements ReflectedShader2 {
    private final CoreElement[] uniforms;
    private final CoreElement[] buffers;

    public CoreReflectedShader(CoreElement[] uniforms, CoreElement[] buffers) {
        this.uniforms = uniforms;
        this.buffers = buffers;
    }

    @Override
    public Iter<ShaderElement> uniforms() {
        return Iter.of(uniforms);
    }

    @Override
    public Iter<ShaderElement> buffers() {
        return Iter.of(buffers);
    }
}
