package com.vke.core.file.exr.head;

import com.vke.api.rendering.abstraction.renderer.IntEnum;
import org.jetbrains.annotations.Nullable;

public enum ExrPixelType implements IntEnum {
    UINT(0, 4),
    HALF(1, 2),
    FLOAT(2, 4);

    public final int repr;
    public final int byteSize;

    ExrPixelType(int repr, int byteSize) {
        this.repr = repr;
        this.byteSize = byteSize;
    }

    @Override
    public int getIntVal() {
        return repr;
    }

    @Override
    public boolean areTheIntegersSorted() {
        return true;
    }

    public ExrPixelType bigger(@Nullable ExrPixelType other) {
        if (other == null) {
            return this;
        }
        if (ordinal() < other.ordinal()) {
            return other;
        }
        return this;
    }
}
