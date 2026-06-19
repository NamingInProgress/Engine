package com.vke.api.rendering.vulkan.descriptors.info;

import com.vke.api.rendering.abstraction.enums.buffer.PackingType;
import com.vke.api.rendering.vulkan.descriptors.DescriptorType;
import com.vke.api.rendering.vulkan.descriptors.types.TypeLayout;
import com.vke.core.vulkan.shr.ReflectedShader;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Objects;

public class BindingLayout {

    public String name;
    public int set;
    public int binding;
    public DescriptorType type;
    public int descriptorCount;
    public boolean multiWrite;

    public BindingLayout(String name, int set, int binding, DescriptorType type, int descriptorCount, boolean multiWrite) {
        this(name, set, binding, type, descriptorCount, null, null, multiWrite);
    }

    public BindingLayout(String name, int set, int binding, DescriptorType type, int descriptorCount,
                         @Nullable TypeLayout typeLayout, @Nullable PackingType packingType, boolean multiWrite) {
        this.name = name;
        this.set = set;
        this.binding = binding;
        this.type = type;
        this.descriptorCount = descriptorCount;
        this.typeLayout = typeLayout;
        this.packingType = packingType;
        this.multiWrite = multiWrite;
    }

    public static BindingLayout fromDescriptorResource(ReflectedShader.DescriptorResource resource, ReflectedShader.ResourceType rt, boolean isDynamic) {
        int count = Arrays.stream(resource.arrayDim).reduce(1, (a, b) -> a * b);
        if (count == 0) {
            count = 1;
        }
        DescriptorType type = DescriptorType.fromBaseType(rt, isDynamic);
        return new BindingLayout(resource.name, resource.set, resource.binding,
                type, count, resource.struct, PackingType.fromDescriptorType(type), resource.multiWrite);
    }

    // Nullable if binding isn't a buffer
    public @Nullable TypeLayout typeLayout;
    public @Nullable PackingType packingType;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BindingLayout that = (BindingLayout) o;

        return set == that.set
                && binding == that.binding
                && descriptorCount == that.descriptorCount
                && Objects.equals(name, that.name)
                && type == that.type
                && Objects.equals(typeLayout, that.typeLayout)
                && packingType == that.packingType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, set, binding, type, descriptorCount, typeLayout, packingType);
    }
}
