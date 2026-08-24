package com.vke.api.rendering.abstraction.renderer;

import java.util.Arrays;

public interface IntEnum {

    int getIntVal();

    static <T extends IntEnum> T fromInt(T[] values, int handle) {
        return Arrays.stream(values).filter(c -> c.getIntVal() == handle).findFirst().orElse(null);
    }

}
