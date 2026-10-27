package com.vke.core.file.exr.head;

import com.vke.api.rendering.abstraction.renderer.IntEnum;

public enum ExrLineOrder implements IntEnum {
    INCERASING_Y(0),
    DECREASING_Y(1),
    RANDOM_Y(2);

    public final int repr;

    ExrLineOrder(int repr) {
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
