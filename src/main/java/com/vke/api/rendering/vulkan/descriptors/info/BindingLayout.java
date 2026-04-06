package com.vke.api.rendering.vulkan.descriptors.info;

import com.vke.api.rendering.abstraction.enums.buffer.PackingType;
import com.vke.api.rendering.vulkan.descriptors.DescriptorType;
import com.vke.api.rendering.vulkan.descriptors.types.TypeLayout;
import org.jetbrains.annotations.Nullable;

public class BindingLayout {

    public String name;
    public int set;
    public int binding;
    public DescriptorType type;
    public int descriptorCount;
    public boolean isDynamic;

    // Nullable if binding isn't a buffer
    public @Nullable TypeLayout typeLayout;
    public @Nullable PackingType packingType;

}
