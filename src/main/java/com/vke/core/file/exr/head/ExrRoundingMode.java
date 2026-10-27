package com.vke.core.file.exr.head;

import com.vke.api.rendering.abstraction.renderer.IntEnum;

public enum ExrRoundingMode implements IntEnum {
    ROUND_DOWN(0),
    ROUND_UP(1);

    public final int repr;

    ExrRoundingMode(int repr) {
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
