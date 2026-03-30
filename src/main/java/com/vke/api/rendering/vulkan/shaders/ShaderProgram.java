package com.vke.api.rendering.vulkan.shaders;

import com.vke.api.rendering.abstraction.shader.Shader;
import com.vke.core.VKEngine;
import com.vke.core.vulkan.device.LogicalDevice;
import com.vke.core.vulkan.shader.VulkanShader;
import com.vke.core.vulkan.shader.ShaderCompiler;
import com.vke.utils.io.Identifier;
import com.vke.utils.tuple.Pair;
import com.vke.utils.Utils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import com.vke.api.rendering.abstraction.enums.ShaderType;

public class ShaderProgram {

    private final HashMap<ShaderType, Identifier> types = new HashMap<>();

    private Shader[] shaders;

    public ShaderProgram(Identifier vertexShaderPath) {
        this(vertexShaderPath, Identifier.empty());
    }

    public ShaderProgram(Identifier vertexShaderPath, Identifier fragmentShaderPath) {
        this(Map.of(ShaderType.VERTEX, vertexShaderPath, ShaderType.FRAGMENT, fragmentShaderPath));
        if (Objects.equals(types.get(ShaderType.FRAGMENT), Identifier.empty())) types.remove(ShaderType.FRAGMENT);
    }

    public ShaderProgram(Pair<ShaderType, Identifier>[] shaders) {
        Arrays.stream(shaders).forEach(pair -> types.put(pair.v1, pair.v2));
    }

    public ShaderProgram(Map<ShaderType, Identifier> shaders) {
        types.putAll(shaders);
    }

    public Shader[] getShaderArray(VKEngine engine, LogicalDevice device, ShaderCompiler compiler) throws Exception {
        if (shaders == null) {
            shaders = new VulkanShader[types.size()];

            int idx = 0;
            for (Map.Entry<ShaderType, Identifier> shaderInfo : types.entrySet()) {
                ShaderType type = shaderInfo.getKey();
                Identifier id = shaderInfo.getValue();
                byte[] bytes = Utils.readAllBytesAndClose(id.asInputStream());
                VulkanShader s = new VulkanShader(engine, device,
                        compiler.compileGlslToSpirV(bytes, type, id),
                        type);
                shaders[idx++] = s;
            }
        }

        return shaders;
    }

}
