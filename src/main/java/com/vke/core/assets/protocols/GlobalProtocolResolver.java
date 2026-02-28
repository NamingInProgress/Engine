package com.vke.core.assets.protocols;

import com.vke.api.assets.AssetHandle;
import com.vke.core.VKEngine;
import com.vke.api.assets.pipeline.AssetPipelineException;
import com.vke.api.assets.pipeline.StageFilter;
import com.vke.api.assets.pipeline.StageElement;
import com.vke.api.assets.pipeline.apis.ProtocolResolver;
import com.vke.utils.Infallible;

public class GlobalProtocolResolver implements ProtocolResolver<Infallible> {
    private final FileProtocolResolver fileProtocolResolver;
    private final MetaProtocolResolver metaProtocolResolver;

    public GlobalProtocolResolver(VKEngine engine) {
        this.fileProtocolResolver = new FileProtocolResolver();
        this.metaProtocolResolver = new MetaProtocolResolver(engine);
    }

    @Override
    public boolean checkProtocolContent(StageFilter filter, StageElement stageElement) throws AssetPipelineException {
        String protocol = filter.getProtocol();
        if ("file".equals(protocol)) {
            return fileProtocolResolver.checkProtocolContent(filter, stageElement);
        } else if ("meta".equals(protocol)) {
            return metaProtocolResolver.checkProtocolContent(filter, stageElement);
        } else {
            throw AssetPipelineException.unknownProtocol(protocol);
        }
    }

    @Override
    public AssetHandle<?> createHandle(StageElement element) throws AssetPipelineException {
        throw new AssetPipelineException("Cannot turn file:// or meta:// into a handle!");
    }

    @Override
    public Infallible resolveData(StageElement element) throws AssetPipelineException {
        throw new AssetPipelineException("Cannot turn file:// or meta:// into data!");
    }
}
