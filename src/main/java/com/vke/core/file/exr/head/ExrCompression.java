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

    public static final ExrCompression[] SUPPORTED = new ExrCompression[] {};

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


}
