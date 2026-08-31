package com.vke.core.color.service;

import com.vke.api.services2.ServiceAPI;
import com.vke.api.services2.ServiceImpl;
import com.vke.core.color.Color;
import com.vke.core.color.convert.ColorConversion;
import org.jetbrains.annotations.Nullable;

public class ColorManagerAPI extends ServiceAPI implements ColorManager {
    public ColorManagerAPI(ServiceImpl baseImpl) {
        super(baseImpl.getId(), baseImpl);
    }

    private ColorManager getImpl() {
        return (ColorManager) getImplementation();
    }

    @Override
    public <DST extends Color> DST convertColor(Color src, Class<DST> dst) {
        return getImpl().convertColor(src, dst);
    }

    @Override
    public <SRC extends Color, DST extends Color> void registerConversion(ColorConversion<SRC, DST> converter) {
        getImpl().registerConversion(converter);
    }

    @Override
    public @Nullable TransferState createTransferState() {
        return getImpl().createTransferState();
    }

    @Override
    public void applyTransferState(@Nullable ColorManager.TransferState state) {
        getImpl().applyTransferState(state);
    }
}
