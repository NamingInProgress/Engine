package com.vke.core.assets.pipeline.protocols.shader;

import com.vke.core.vulkan.service.VulkanRenderer;
import com.vke.utils.Utils;
import com.vke.utils.io.Identifier;
import com.vke.utils.tuple.Pair;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class ShaderPreprocessor {

    private static ShaderPreprocessor instance;

    public static ShaderPreprocessor getInstance(VulkanRenderer renderer) {
        if (instance == null) instance = new ShaderPreprocessor(renderer);
        return instance;
    }

    private final VulkanRenderer renderer;

    public ShaderPreprocessor(VulkanRenderer renderer) {
        this.renderer = renderer;
    }

    public Pair<String, ShaderMetadata> process(Identifier ident) throws IOException {
        String code = Utils.readStringFromInputStream(ident.asInputStream());
        code = code.replace("#MultipleWrites(100)", "");
        code = code.replace("#Static", "");
        code = code.replace("#DefaultSize(1024)", "");

        var meta = new ShaderMetadata(new HashMap<>(), new ArrayList<>(), new HashMap<>());
        meta.defaultRuntimeSizes.put("modelMatrices", 10);
        meta.defaultRuntimeSizes.put("textures", renderer.getBindlessTexturesCount());
        return new Pair<>(code, meta);
    }

    public record ShaderMetadata(HashMap<String, Integer> multipleWrites, ArrayList<String> staticBuffers, HashMap<String, Integer> defaultRuntimeSizes) {}

}
