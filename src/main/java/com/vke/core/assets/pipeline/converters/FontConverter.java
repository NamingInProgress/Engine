package com.vke.core.assets.pipeline.converters;

import com.carrotsearch.hppc.ObjectArrayList;
import com.vke.api.assets.Protocols;
import com.vke.api.assets.anot.Converter;
import com.vke.api.font.Font;
import com.vke.api.font.FontFaceImpl;
import com.vke.api.parsing.config.node.ConfigArrayNode;
import com.vke.api.vkz.VkzArchive;
import com.vke.api.vkz.VkzFileHandle;
import com.vke.core.Context;
import com.vke.core.assets.AssetException;
import com.vke.core.assets.pipeline.StageElement;
import com.vke.core.assets.pipeline.apis.AssetConverter;
import com.vke.core.assets.pipeline.apis.AssetData;
import com.vke.core.font.ttf.TTFFont;
import com.vke.utils.Utils;
import com.vke.utils.iter.Iter;

@Converter
public class FontConverter implements AssetConverter {
    @Override
    public String from() {
        return Protocols.VKZ;
    }

    @Override
    public String to() {
        return Protocols.FONT;
    }

    @Override
    public AssetData performConversion(Context context, StageElement input, ConfigArrayNode arguments) throws AssetException {
        return Utils.chainExceptions(() -> {
            VkzArchive open = input.getAssetData().getDataAs();
            Iter<VkzFileHandle> iter = open.iterateFiles();

            ObjectArrayList<Font> fonts = new ObjectArrayList<>();
            for (VkzFileHandle vkzFileHandle : iter) {
                TTFFont font = new TTFFont(vkzFileHandle.getInputStream());
                fonts.add(font);
            }

            return new AssetData(Protocols.FONT, new FontFaceImpl(fonts.toArray(Font.class)));
        });
    }
}
