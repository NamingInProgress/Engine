package com.vke.api.abstraction;

public interface IntBitEnum<SELF, BITS extends IntEnum> extends IntEnum {
    SELF or(BITS... bits);
}
