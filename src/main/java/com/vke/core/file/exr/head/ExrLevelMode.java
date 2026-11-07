package com.vke.core.file.exr.head;

import com.vke.api.rendering.abstraction.renderer.IntEnum;
import com.vke.core.color.Color;
import com.vke.core.file.exr.ExrUtils;

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

    public int numberOfLevelsX(int w, int h, ExrRoundingMode mode) {
        return switch (this) {
            case ONE_LEVEL -> 1;
            case MIPMAP_LEVELS -> ExrUtils.ilog2(Math.max(w, h), mode) + 1;
            case RIPMAP_LEVELS -> ExrUtils.ilog2(w, mode) + 1;
        };
    }

    public int numberOfLevelsY(int w, int h, ExrRoundingMode mode) {
        return switch (this) {
            case ONE_LEVEL -> 1;
            case MIPMAP_LEVELS -> ExrUtils.ilog2(Math.max(w, h), mode) + 1;
            case RIPMAP_LEVELS -> ExrUtils.ilog2(h, mode) + 1;
        };
    }

    //i took this from here cuz i CBA to think about this https://github.com/AcademySoftwareFoundation/openexr/blob/238a4b69fd640edcb671f31c23fd23abacbb4a8b/src/lib/OpenEXR/ImfTiledMisc.cpp#L27
    //explanation: this calculates the size of a level in a direction. the extent is either the total width or height of the dataWindow
    public static int levelSize(int extent, int level, ExrRoundingMode mode) {
        int b = (1 << level);
        int size = extent / b;
        if (mode == ExrRoundingMode.ROUND_UP && size * b < extent) size++;
        return Math.max(size, 1);
    }
}
