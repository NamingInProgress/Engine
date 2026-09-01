package com.vke.core.color.convert;

import com.vke.core.color.Color;

public interface ColorConversion<SRC extends Color, DST extends Color> {
    @SuppressWarnings("unchecked")
    default Class<SRC> getSrc(SRC... ignore) {
        return (Class<SRC>) ignore.getClass().getComponentType();
    }

    @SuppressWarnings("unchecked")
    default Class<DST> getDst(DST... ignore) {
        return (Class<DST>) ignore.getClass().getComponentType();
    }

    DST convert(SRC src);
}
