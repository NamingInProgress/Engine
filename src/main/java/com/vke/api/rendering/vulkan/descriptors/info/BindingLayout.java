package com.vke.api.rendering.vulkan.descriptors.info;

import com.vke.api.rendering.vulkan.descriptors.DescriptorType;
import com.vke.api.rendering.vulkan.descriptors.types.TypeLayout;

public class BindingLayout {

    public String name;
    public int set;
    public int binding;
    public DescriptorType type;
    public int descriptorCount;
    public boolean isDynamic;

    public TypeLayout typeLayout;

}
