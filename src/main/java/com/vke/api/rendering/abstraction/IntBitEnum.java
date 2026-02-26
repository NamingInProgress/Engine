package com.vke.api.rendering.abstraction;

public interface IntBitEnum<SELF, BITS extends IntEnum> extends IntEnum {
    SELF or(BITS... bits);
}
