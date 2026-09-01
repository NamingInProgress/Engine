package com.vke.core.color.service;

import com.vke.api.services2.ServiceImpl;
import com.vke.core.VKEngine;
import com.vke.core.color.Color;
import com.vke.core.color.RgbColor;
import com.vke.core.color.convert.ColorConversion;
import com.vke.core.services2.Services;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.List;

public class ColorManagerImpl extends ServiceImpl implements ColorManager {
    private TransferState state;

    public ColorManagerImpl(VKEngine engine) {
        super(Services.COLOR, engine);
    }

    @Override
    protected void onInitialize() {
        this.state = new TransferState(new HashMap<>());
    }

    @Override
    @SuppressWarnings("unchecked")
    public <DST extends Color> DST convertColor(Color src, Class<DST> dst) {
        var map = state.conversions().get(src.getClass());
        if (map != null) {
            ColorConversion<Color, DST> conv = (ColorConversion<Color, DST>) map.get(dst);
            if (conv != null) {
                return conv.convert(src);
            }
        }
        RgbColor rgb = src.toRgb();
        try {
            Constructor<DST> constructor = dst.getDeclaredConstructor(RgbColor.class);
            return constructor.newInstance(rgb);
        } catch (Exception ignored) {
            return null;
        }
    }

    @Override
    @SuppressWarnings("all")
    public <SRC extends Color, DST extends Color> void registerConversion(ColorConversion<SRC, DST> converter) {
        var src = converter.getSrc();
        var dst = converter.getDst();

        var map = state.conversions().get(src);
        if (map == null) {
            map = new HashMap<>();
            state.conversions().put(src, map);
        }

        map.put(dst, converter);
    }

    @Override
    public @Nullable TransferState createTransferState() {
        return state;
    }

    @Override
    public void applyTransferState(@Nullable ColorManager.TransferState state) {
        this.state = state;
    }

    @Override
    public List<String> dependencies() {
        return List.of();
    }

    @Override
    public void free() {

    }
}
