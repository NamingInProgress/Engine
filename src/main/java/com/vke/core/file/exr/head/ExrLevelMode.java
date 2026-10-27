package com.vke.core.file.exr.head;

import com.vke.api.rendering.abstraction.renderer.IntEnum;

public enum ExrLevelMode implements IntEnum {
    ONE_LEVEL(0),
    MIPMAP_LEVELS(1),
    RIPMAP_LEVELS(2);

    public final int repr;

    ExrLevelMode(int repr) {
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
