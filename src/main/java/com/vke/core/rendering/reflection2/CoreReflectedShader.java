package com.vke.core.rendering.reflection2;

import com.vke.api.rendering.abstraction.renderer.enums.ShaderType;
import com.vke.core.assets.pipeline.protocols.shader.ShaderPreprocessor;
import com.vke.core.rendering.reflection2.api.*;
import com.vke.utils.io.Identifier;
import com.vke.utils.iter.Iter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CoreReflectedShader implements ReflectedShader2 {
    private final ShaderType type;
    private final ShaderPreprocessor.ShaderMetadata meta;
    private final Identifier ident;

    private final PushConstantsResource pushConstantsResource;
    private final Map<DescriptorCategory,  List<DescriptorResource>> descriptorResources;
    private final List<VertexAttributeResource> vertexAttributeResources;

    public CoreReflectedShader(Identifier ident, ShaderType shaderType, ShaderPreprocessor.ShaderMetadata metadata, SpirvItem pushConstants, List<SpirvItem> descriptors, List<SpirvItem> vaos) {
        this.type = shaderType;
        this.meta = metadata;
        this.ident = ident;

        if (pushConstants != null) {
            this.pushConstantsResource = new PushConstantsResource(pushConstants);
        } else {
            this.pushConstantsResource = null;
        }

        this.descriptorResources = new HashMap<>();
        for (SpirvItem item : descriptors) {
            DescriptorResource res = new DescriptorResource(item, meta);
            DescriptorCategory category = item.category;
            List<DescriptorResource> list = descriptorResources.computeIfAbsent(category, _ -> new ArrayList<>());
            list.add(res);
        }

        this.vertexAttributeResources = Iter.of(vaos)
                .map(VertexAttributeResource::new)
                .collectToList();
    }

    @Override
    public PushConstantsResource pushConstants() {
        return pushConstantsResource;
    }

    @Override
    public Map<DescriptorCategory, List<DescriptorResource>> descriptors() {
        return descriptorResources;
    }

    @Override
    public List<VertexAttributeResource> vertexAttributes() {
        return vertexAttributeResources;
    }

    @Override
    public ShaderType getShaderType() {
        return type;
    }

    @Override
    public ShaderPreprocessor.ShaderMetadata getMetadata() {
        return meta;
    }

    @Override
    public Identifier getIdentifier() {
        return ident;
    }

    @Override
    public void free() {

    }
}
