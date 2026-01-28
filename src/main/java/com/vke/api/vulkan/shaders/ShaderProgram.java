package com.vke.api.vulkan.shaders;

import com.vke.core.VKEngine;
import com.vke.core.rendering.vulkan.device.LogicalDevice;
import com.vke.core.rendering.vulkan.shader.Shader;
import com.vke.core.rendering.vulkan.shader.ShaderCompiler;
import com.vke.utils.Identifier;
import com.vke.utils.Pair;
import com.vke.utils.Utils;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static com.vke.core.rendering.vulkan.shader.Shader.Type;

public class ShaderProgram {

    private final HashMap<Type, Identifier> types = new HashMap<>();

    private Shader[] shaders;

    public ShaderProgram(Identifier vertexShaderPath) {
        this(vertexShaderPath, Identifier.empty());
    }

    public ShaderProgram(Identifier vertexShaderPath, Identifier fragmentShaderPath) {
        this(Map.of(Type.VERTEX, vertexShaderPath, Type.FRAGMENT, fragmentShaderPath));
        if (types.get(Type.FRAGMENT) == Identifier.empty()) types.remove(Type.FRAGMENT);
    }

    public ShaderProgram(Pair<Type, Identifier>[] shaders) {
        Arrays.stream(shaders).forEach(pair -> types.put(pair.v1, pair.v2));
    }

    public ShaderProgram(Map<Type, Identifier> shaders) {
        types.putAll(shaders);
    }

    public Shader[] getShaderArray(VKEngine engine, LogicalDevice device, ShaderCompiler compiler) throws Exception {
        if (shaders == null) {
            shaders = new Shader[types.size()];

            int idx = 0;
            for (Map.Entry<Type, Identifier> shaderInfo : types.entrySet()) {
                Type type = shaderInfo.getKey();
                Identifier id = shaderInfo.getValue();
                byte[] bytes = Utils.readAllBytesAndClose(id.asInputStream());
                Shader s = new Shader(engine, device,
                        compiler.compileGlslToSpirV(bytes, type, type.toString()),
                        type);
                shaders[idx++] = s;
            }
        }

        return shaders;
    }

}
