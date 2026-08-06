package com.vke.core.assets.pipeline.protocols.texture;

import com.vke.api.assets.Protocols;
import com.vke.api.assets.anot.Protocol;
import com.vke.api.rendering.abstraction.renderer.data.Texture;
import com.vke.core.assets.pipeline.apis.AbstractAssetProtocol;

@Protocol
public class TextureProtocol implements AbstractAssetProtocol<Texture> {
    @Override
    public String getProtocolName() {
        return Protocols.TEXTURE;
    }
}
