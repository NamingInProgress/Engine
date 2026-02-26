package com.vke.api.rendering.abstraction.shader;

import com.vke.api.rendering.abstraction.enums.ShaderType;
import com.vke.utils.Disposable;

public interface Shader extends Disposable {
    ShaderType type();
}
