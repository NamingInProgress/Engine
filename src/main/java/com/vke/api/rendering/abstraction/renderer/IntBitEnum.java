package com.vke.api.rendering.abstraction.renderer;

public interface IntBitEnum<SELF, BITS extends IntEnum> extends IntEnum {
    SELF or(BITS... bits);
}
