package com.vke.core.file.exr.head;

import com.vke.api.rendering.abstraction.renderer.IntEnum;

public enum ExrCompression implements IntEnum {
    NO(0),
    RLE(1),
    ZIPS(2),
    ZIP(3),
    PIZ(4),
    PXR24(5),
    B44(6),
    B44A(7),
    DWAA(8),
    DWAB(9),
    HTJ2K256(10),
    HTJ2K32(11);

    public final int repr;

    ExrCompression(int repr) {
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

    @SuppressWarnings("all") //for the cases with same value but imo this looks cleaner
    public int numberOfScanlinesPerBlock() {
        return switch (this) {
            case NO -> 1;
            case RLE -> 1;
            case ZIPS -> 1;
            case ZIP -> 16;
            case PIZ -> 32;
            case PXR24 -> 16;
            case B44 -> 32;
            case B44A -> 32;
            case DWAA -> 32;
            case DWAB -> 256;
            case HTJ2K256 -> 256;
            case HTJ2K32 -> 32;
        };
    }
}
