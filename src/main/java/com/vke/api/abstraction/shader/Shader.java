package com.vke.api.abstraction.shader;

import com.vke.api.abstraction.descriptors.ShaderType;
import com.vke.utils.Disposable;

public interface Shader extends Disposable {
    ShaderType type();
}
