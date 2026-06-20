package com.vke.core.rendering.reflection2;

import com.vke.api.rendering.abstraction.enums.buffer.PackingType;
import com.vke.core.rendering.reflection2.api.ShaderElement;
import com.vke.utils.iter.Iter;
import com.vke.utils.iter.helpers.Option;

public class CoreElement implements ShaderElement {
    @Override
    public Type type() {
        return null;
    }

    @Override
    public String typeRepr() {
        return "";
    }

    @Override
    public int sizeBytes(PackingType packingType) {
        return 0;
    }

    @Override
    public Iter<ShaderElement> children() {
        return null;
    }

    @Override
    public int[] dimensions() {
        return new int[0];
    }

    @Override
    public VulkanExtra extra() {
        return null;
    }

    @Override
    public Option<String> definitionName() {
        return null;
    }
}
