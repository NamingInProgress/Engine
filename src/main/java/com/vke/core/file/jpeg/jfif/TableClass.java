package com.vke.core.file.jpeg.jfif;

import com.vke.api.rendering.abstraction.renderer.IntEnum;

public enum TableClass implements IntEnum {
    DctOrLossless(0),
    Ari(1);

    private final int val;

    TableClass(int val) {
        this.val = val;
    }

    @Override
    public int getIntVal() {
        return val;
    }
}
