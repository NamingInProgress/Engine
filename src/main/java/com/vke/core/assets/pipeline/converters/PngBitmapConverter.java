package com.vke.core.assets.pipeline.converters;

import com.vke.api.assets.Protocols;
import com.vke.api.assets.anot.Converter;
import com.vke.api.parsing.config.node.ConfigArrayNode;
import com.vke.core.Context;
import com.vke.core.assets.AssetException;
import com.vke.core.assets.pipeline.StageElement;
import com.vke.core.assets.pipeline.apis.AssetConverter;
import com.vke.core.assets.pipeline.apis.AssetData;
import com.vke.core.file.png.PngFile;
import com.vke.core.file.png.PngInfo;

@Converter
public class PngBitmapConverter implements AssetConverter {
    @Override
    public String from() {
        return Protocols.PNG;
    }

    @Override
    public String to() {
        return Protocols.BITMAP;
    }

    @Override
    public AssetData performConversion(Context context, StageElement input, ConfigArrayNode arguments) throws AssetException {
        PngFile png = input.getAssetData().getDataAs();
        return new AssetData(Protocols.BITMAP, png.getOutput());
    }
}
