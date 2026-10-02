package com.vke.core.assets.pipeline.protocols;

import com.vke.api.assets.Protocols;
import com.vke.api.assets.anot.Protocol;
import com.vke.api.font.FontFace;
import com.vke.core.assets.pipeline.apis.AbstractAssetProtocol;

@Protocol
public class FontProtocol implements AbstractAssetProtocol<FontFace> {
    @Override
    public String getProtocolName() {
        return Protocols.FONT;
    }
}
