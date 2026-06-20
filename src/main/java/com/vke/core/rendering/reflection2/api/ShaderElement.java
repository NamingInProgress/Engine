package com.vke.core.rendering.reflection2.api;

import com.vke.api.rendering.abstraction.enums.buffer.PackingType;
import com.vke.utils.iter.Iter;
import com.vke.utils.iter.helpers.Option;

public interface ShaderElement {
    Type type();
    String typeRepr();

    int sizeBytes(PackingType packingType);
    Iter<ShaderElement> children();

    int[] dimensions();
    default ShaderElement component() {
        return children().next().expect("ShaderElement of type Array must have at least 1 child!");
    }

    Object extra();

    Option<String> definitionName();

    enum Type {
        Complex,
        Array,

        Vec,
        Mat,

        Int,
        Float,
        Sampler,

        Verbatim
    }
}
