package com.vke.core.assets.pipeline.protocols.texture;

import com.vke.api.assets.Protocols;
import com.vke.api.assets.anot.Protocol;
import com.vke.core.assets.AssetException;
import com.vke.core.assets.pipeline.Op;
import com.vke.core.assets.pipeline.StageElement;
import com.vke.core.assets.pipeline.apis.AssetData;
import com.vke.core.assets.pipeline.apis.AssetProtocol;
import com.vke.core.assets.pipeline.apis.AssetUri;
import com.vke.core.assets.pipeline.protocols.loader.UnsupportedLoader;
import com.vke.core.image.Bitmap;
import com.vke.utils.Utils;

@Protocol
public class BitmapProtocol implements AssetProtocol<Bitmap> {
    @Override
    public String getProtocolName() {
        return Protocols.BITMAP;
    }

    @Override
    public AssetData getField(StageElement data, AssetUri uri) throws AssetException {
        Router router = new Router(getProtocolName());
        router.addPath(Utils.p("format"), this::getFormatFor);
        return router.getDataAtSafe(uri.getSegments(), data);
    }

    private AssetData getFormatFor(StageElement e) {
        Bitmap map = e.getAssetData(getProtocolName()).getDataAs();
        return AssetData.plain(map.dataFormat().name());
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
