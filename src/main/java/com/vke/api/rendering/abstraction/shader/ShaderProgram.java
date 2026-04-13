package com.vke.api.rendering.abstraction.shader;

import com.vke.api.assets.AssetHandle;
import com.vke.utils.io.Identifier;

public class ShaderProgram {

    private AssetHandle<Shader>[] shaders;
    private Identifier[] identifiers;

    public ShaderProgram(AssetHandle<Shader> shader, Identifier identifier) {
        this(new AssetHandle[]{ shader },  new Identifier[]{ identifier });
    }

    public ShaderProgram(AssetHandle<Shader>[] shaders, Identifier[] identifiers) {
        this.shaders = shaders;
        this.identifiers = identifiers;
    }

    public AssetHandle<Shader>[] getShaders() {
        return this.shaders;
    }
    public Identifier[] getIdentifiers() {
        return this.identifiers;
    }
    public int getShaderCount() { return this.shaders.length; }

}
