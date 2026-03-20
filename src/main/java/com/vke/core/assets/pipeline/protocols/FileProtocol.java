package com.vke.core.assets.pipeline.protocols;

import com.vke.api.assets.Protocols;
import com.vke.core.assets.pipeline.AssetPipelineException;
import com.vke.core.assets.pipeline.Op;
import com.vke.core.assets.pipeline.StageElement;
import com.vke.core.assets.pipeline.apis.AssetData;
import com.vke.core.assets.pipeline.apis.AssetProtocol;
import com.vke.core.assets.pipeline.apis.AssetUri;
import com.vke.core.assets.pipeline.protocols.loader.UnsupportedLoader;
import com.vke.utils.Infallible;
import com.vke.utils.Utils;
import com.vke.utils.io.FileUtils;

public class FileProtocol implements AssetProtocol<Infallible> {
    private final Router router;

    public FileProtocol() {
        router = new Router(getProtocolName());
        router.addPath(Utils.p("extension"), this::getExtension);
        router.addPath(Utils.p("name"), this::getName);
        router.addPath(Utils.p("nickname"), this::getNickname);
        router.addPath(Utils.p("location"), this::getLocation);
        router.addPath(Utils.p("location", "parent"), this::getLocationParent);
        router.addPath(Utils.p("location", "namespace"), this::getLocationNamespace);
    }

    @Override
    public String getProtocolName() {
        return Protocols.FILE;
    }

    @Override
    public AssetData getField(AssetData data, AssetUri uri) throws AssetPipelineException {
        return router.getDataAtSafe(uri.getSegments(), data.getDataAs());
    }

    @Override
    public Loader getLoader() {
        return new UnsupportedLoader(getProtocolName());
    }

    @Override
    public boolean applies(AssetData a, AssetData b, Op op) {
        return false;
    }

    private AssetData getExtension(StageElement element) {
        return AssetData.plain(FileUtils.getExtension(element.getPath()));
    }

    private AssetData getName(StageElement element) {
        return AssetData.plain(FileUtils.getFileName(element.getPath()));
    }

    private AssetData getNickname(StageElement element) {
        return AssetData.plain(FileUtils.getFileNickname(element.getPath()));
    }

    private AssetData getLocation(StageElement element) {
        return AssetData.path(element.getPath());
    }

    private AssetData getLocationParent(StageElement element) {
        return AssetData.plain(FileUtils.getComponent(element.getPath(), -1));
    }

    private AssetData getLocationNamespace(StageElement element) {
        return AssetData.plain(FileUtils.getComponent(element.getPath(), 0));
    }
}
