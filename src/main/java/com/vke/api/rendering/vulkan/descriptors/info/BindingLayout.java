package com.vke.api.rendering.vulkan.descriptors.info;

import com.vke.api.rendering.abstraction.enums.buffer.PackingType;
import com.vke.api.rendering.vulkan.descriptors.DescriptorType;
import com.vke.api.rendering.vulkan.descriptors.types.TypeLayout;
import com.vke.core.services.shr.ReflectedShader;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

public class BindingLayout {

    public String name;
    public int set;
    public int binding;
    public DescriptorType type;
    public int descriptorCount;

    public BindingLayout(String name, int set, int binding, DescriptorType type, int descriptorCount) {
        this(name, set, binding, type, descriptorCount, null, null);
    }

    public BindingLayout(String name, int set, int binding, DescriptorType type, int descriptorCount,
                         @Nullable TypeLayout typeLayout, @Nullable PackingType packingType) {
        this.name = name;
        this.set = set;
        this.binding = binding;
        this.type = type;
        this.descriptorCount = descriptorCount;
        this.typeLayout = typeLayout;
        this.packingType = packingType;
    }

    public static BindingLayout fromDescriptorResource(ReflectedShader.DescriptorResource resource, ReflectedShader.ResourceType rt, boolean isDynamic) {
        int count = Arrays.stream(resource.arrayDim).reduce(1, (a, b) -> a * b);
        DescriptorType type = DescriptorType.fromBaseType(rt, isDynamic);
        return new BindingLayout(resource.name, resource.set, resource.binding,
                type, count, resource.struct, PackingType.fromDescriptorType(type));
    }

    // Nullable if binding isn't a buffer
    public @Nullable TypeLayout typeLayout;
    public @Nullable PackingType packingType;

}
