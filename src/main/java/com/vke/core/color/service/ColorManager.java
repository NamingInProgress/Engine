package com.vke.core.color.service;

import com.vke.api.services2.StatefulService;
import com.vke.core.color.Color;
import com.vke.core.color.convert.ColorConversion;
import com.vke.core.color.convert.ColorConversion2Way;

import java.util.Map;

public interface ColorManager extends StatefulService<ColorManager.TransferState> {
    <DST extends Color> DST convertColor(Color src, Class<DST> dst);

    @SuppressWarnings("unchecked")
    default <DST extends Color> DST convertColor(Color src, DST... ignore) {
        return convertColor(src, (Class<DST>) ignore.getClass().getComponentType());
    }

    <SRC extends Color, DST extends Color> void registerConversion(ColorConversion<SRC, DST> converter);
    default <A extends Color, B extends Color> void register2WayConversion(ColorConversion2Way<A, B> converter) {
        registerConversion(converter);
        registerConversion(converter.back());
    }

    record TransferState(Map<Class<? extends Color>, Map<Class<? extends Color>, ColorConversion<?, ?>>> conversions) {
    }
}
