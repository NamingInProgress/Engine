package com.vke.core.file.exr;

import com.vke.core.file.exr.head.ExrRoundingMode;
import com.vke.utils.IntUtils;

public class ExrUtils {
    public static int ilog2(int v, ExrRoundingMode roundingMode) {
        return roundingMode == ExrRoundingMode.ROUND_UP ? IntUtils.fastiLogCeil2(v) : IntUtils.fastiLogFloor2(v);
    }
}
