package com.vke.api.rendering.abstraction.renderer.shader;

import com.vke.api.rendering.abstraction.renderer.enums.ShaderType;
import com.vke.utils.io.Disposable;

public interface Shader extends Disposable {
    ShaderType type();
}
