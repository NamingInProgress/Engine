package com.vke.core.assets.pipeline.protocols.texture;

import com.vke.api.assets.Protocols;
import com.vke.api.assets.anot.Protocol;
import com.vke.api.rendering.abstraction.renderer.RenderDevice;
import com.vke.api.rendering.abstraction.renderer.Renderer;
import com.vke.api.rendering.abstraction.renderer.data.Texture;
import com.vke.api.rendering.abstraction.renderer.enums.texture.Format;
import com.vke.api.rendering.abstraction.renderer.enums.texture.ImageUsage;
import com.vke.api.rendering.abstraction.renderer.enums.texture.TextureType;
import com.vke.core.Context;
import com.vke.core.EngineCreateInfo;
import com.vke.core.FileIdentifier;
import com.vke.core.assets.AssetException;
import com.vke.core.assets.pipeline.Op;
import com.vke.core.assets.pipeline.apis.AssetData;
import com.vke.core.assets.pipeline.apis.AssetProtocol;
import com.vke.core.assets.pipeline.apis.AssetUri;
import com.vke.core.assets.pipeline.protocols.loader.UnsupportedLoader;
import com.vke.core.assets.pipeline.stages.PipelineStage;
import com.vke.core.file.png.Pixels;
import com.vke.core.file.png.PngFile;
import com.vke.utils.Utils;

@Protocol
public class PngProtocol implements AssetProtocol<Texture> {
    @Override
    public String getProtocolName() {
        return Protocols.PNG;
    }

    @Override
    public AssetData getField(AssetData data, AssetUri uri) throws AssetException {
        throw AssetException.unknownSelector(getProtocolName(), uri.getSelector());
    }

    @Override
    public Loader getLoader() {
        return new UnsupportedLoader(getProtocolName());
    }

    @Override
    public boolean applies(AssetData a, AssetData b, Op op) {
        return false;
    }
}
