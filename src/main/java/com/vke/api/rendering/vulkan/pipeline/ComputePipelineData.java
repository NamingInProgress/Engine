package com.vke.api.rendering.vulkan.pipeline;

import com.vke.api.assets.r.R;
import com.vke.api.parsing.config.ConfigDocument;
import com.vke.api.parsing.config.node.ConfigArrayNode;
import com.vke.api.parsing.config.node.ConfigNode;
import com.vke.api.parsing.config.node.ConfigObjectNode;
import com.vke.api.rendering.abstraction.renderer.shader.ShaderProgram;
import com.vke.api.rendering.vulkan.descriptors.info.DescriptorsInfo;
import com.vke.core.rendering.vulkan.shader.VKShaderProgram;
import com.vke.utils.io.Identifier;
import com.vke.utils.iter.helpers.Option;

public class ComputePipelineData {

    // WARNING! THESE FIELDS ARE RESOLVED ONLY DURING PIPELINE CREATION, WHICH MEANS THEY WILL NOT BE AVAILABLE BEFOREHAND!
    public DescriptorsInfo additionalDescriptorInfo;
    public VKShaderProgram compiledShaders;

    public ShaderProgram shader;

    private static final String
            SHADER_NAME = "shader",
            DYNAMIC_BUFFERS_ARRAY_NAME = "dynamicBuffers",
            RUNTIME_SIZE_ARRAYS_NAME = "runtimeSizeArrays";

    private static final String
            RUNTIME_SIZE_ARRAYS_NAME_NAME = "name",
            RUNTIME_SIZE_ARRAYS_SIZE_NAME = "size";

    public static ComputePipelineData fromConfig(ConfigDocument document) {
        ConfigNode root = document.getRoot();
        ComputePipelineData cd = new ComputePipelineData();
        cd.shader = shaders(root);
        cd.additionalDescriptorInfo = descriptorsInfo(root);

        return cd;
    }

    private static ShaderProgram shaders(ConfigNode parent) {
        Option<String> shaderIdentPath = parent.getStringOption(SHADER_NAME);
        if (shaderIdentPath.isNone()) throw new IllegalStateException("Cannot create compute pipeline without a shader!");
        Identifier ident = Identifier.of(shaderIdentPath.unwrap());

        return new ShaderProgram(R.shaders.get(ident), ident);
    }

    public static DescriptorsInfo descriptorsInfo(ConfigNode parent) {
        DescriptorsInfo di = new DescriptorsInfo();

        Option<ConfigArrayNode> dynamicBufferOptional = parent.getArrayOption(DYNAMIC_BUFFERS_ARRAY_NAME);
        if (dynamicBufferOptional.isSome()) {
            for (ConfigNode n : dynamicBufferOptional.unwrap().values()) {
                String name = n.asString();
                di.dynamicBuffers.add(name);
            }
        }

        Option<ConfigArrayNode> runtimeSizeArraysOptional = parent.getArrayOption(RUNTIME_SIZE_ARRAYS_NAME);
        if (runtimeSizeArraysOptional.isSome()) {
            for (ConfigNode n : runtimeSizeArraysOptional.unwrap().values()) {
                ConfigObjectNode obj = n.asObject();
                String name = obj.getString(RUNTIME_SIZE_ARRAYS_NAME_NAME);
                int size = obj.getInt(RUNTIME_SIZE_ARRAYS_SIZE_NAME);

                if (size < 1) throw new IllegalStateException("Cannot create runtime size array of size " + size);

                di.runtimeSizeArraySizes.put(name, size);
            }
        }

        return di;
    }

}
