package com.vke.api.rendering.vulkan.descriptors.types;

import java.util.Objects;

public abstract class TypeLayout {

    public String name;
    public long size;

    @Override
    public abstract boolean equals(Object other);

    @Override
    public int hashCode() {
        return Objects.hash(name, size);
    }
}
