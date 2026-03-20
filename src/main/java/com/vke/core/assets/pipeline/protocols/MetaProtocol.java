package com.vke.core.assets.pipeline.protocols;

import com.vke.api.assets.Protocols;
import com.vke.core.VKEngine;
import com.vke.core.assets.pipeline.AssetPipelineException;
import com.vke.core.assets.pipeline.Op;
import com.vke.core.assets.pipeline.StageElement;
import com.vke.core.assets.pipeline.apis.AssetData;
import com.vke.core.assets.pipeline.apis.AssetProtocol;
import com.vke.core.assets.pipeline.apis.AssetUri;
import com.vke.core.assets.pipeline.protocols.loader.UnsupportedLoader;
import com.vke.utils.Infallible;
import com.vke.utils.Utils;

public class MetaProtocol implements AssetProtocol<Infallible> {
    private final VKEngine engine;
    private final Router router;

    public MetaProtocol(VKEngine engine) {
        this.engine = engine;
        router = new Router(getProtocolName());
        router.addPath(Utils.p("os"), this::getOs);
        router.addPath(Utils.p("build"), this::getBuild);
    }

    @Override
    public String getProtocolName() {
        return Protocols.META;
    }

    @Override
    public AssetData getField(AssetData data, AssetUri uri) throws AssetPipelineException {
        return null;
    }

    @Override
    public Loader getLoader() {
        return new UnsupportedLoader(getProtocolName());
    }

    @Override
    public boolean applies(AssetData a, AssetData b, Op op) {
        return false;
    }

    private AssetData getOs(StageElement element) {
        return AssetData.plain(System.getProperty("os.name").toLowerCase());
    }

    private AssetData getBuild(StageElement element) {
        return AssetData.plain(engine.isDebugMode() ? "debug" : "release");
    }
}
