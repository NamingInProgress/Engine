package com.vke.api.rendering.abstraction.renderer;

import java.util.Arrays;

public interface IntEnum {

    int getVkHandle();

    static <T extends IntEnum> T fromInt(T[] values, int handle) {
        return Arrays.stream(values).filter(c -> c.getVkHandle() == handle).findFirst().orElse(null);
    }

}
