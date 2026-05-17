package com.vke.api.rendering.vulkan.descriptors.info;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

public class DescriptorSetLayout {

    public HashSet<BindingLayout> bindings = new HashSet<>();

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        DescriptorSetLayout that = (DescriptorSetLayout) o;
        return bindings.equals(that.bindings);
    }

}
