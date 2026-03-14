package com.vke.core.assets.protocols;

import com.vke.api.assets.AssetHandle;
import com.vke.core.VKEngine;
import com.vke.api.assets.pipeline.AssetPipelineException;
import com.vke.api.assets.pipeline.StageFilter;
import com.vke.api.assets.pipeline.StageElement;
import com.vke.api.assets.pipeline.apis.ProtocolResolver;
import com.vke.utils.Infallible;

import java.net.URI;

public class MetaProtocolResolver implements ProtocolResolver<Infallible> {
    private final VKEngine engine;

    public MetaProtocolResolver(VKEngine engine) {
        this.engine = engine;
    }

    @Override
    public boolean checkProtocolContent(StageFilter filter, StageElement stageElement) throws AssetPipelineException {
        String selector = filter.getSelector();
        return switch (selector) {
            case "os" -> checkOs(filter);
            case "build" -> checkBuild(filter);
            default -> throw AssetPipelineException.unknownSelector("meta", selector);
        };
    }

    @Override
    public String resolveUri(URI uri, StageElement stageElement) throws AssetPipelineException {
        String selector = uri.getAuthority();
        return switch (selector) {
            case "os" -> System.getProperty("os.name").toLowerCase();
            case "build" -> engine.isDebugMode() ? "debug" : "release";
            default -> throw AssetPipelineException.unknownSelector("meta", selector);
        };
    }

    @Override
    public AssetHandle<?> createHandle(StageElement element) throws AssetPipelineException {
        throw new AssetPipelineException("Cannot turn meta:// into a handle!");
    }

    @Override
    public Infallible resolveData(StageElement element) throws AssetPipelineException {
        throw new AssetPipelineException("Cannot turn meta:// into data!");
    }

    private boolean checkOs(StageFilter filter) {
        String os = System.getProperty("os.name").toLowerCase();
        return filter.applyForString(os);
    }

    private boolean checkBuild(StageFilter filter) {
        String buildMode = engine.isDebugMode() ? "debug" : "release";
        return filter.applyForString(buildMode);
    }
}
