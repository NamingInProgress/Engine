package com.vke.core.file.exr.head;

import com.vke.api.rendering.abstraction.renderer.IntEnum;

import java.util.Arrays;

public enum ExrPixelType implements IntEnum {
    UINT(0),
    HALF(1),
    FLOAT(2);

    public final int repr;

    ExrPixelType(int repr) {
        this.repr = repr;
    }

    @Override
    public int getIntVal() {
        return repr;
    }

    @Override
    public boolean areTheIntegersSorted() {
        return true;
    }
}
