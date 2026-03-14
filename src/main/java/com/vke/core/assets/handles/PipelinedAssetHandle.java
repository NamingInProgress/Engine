package com.vke.core.assets.handles;

import com.vke.api.assets.AssetHandle;
import com.vke.api.assets.pipeline.AssetPipeline;
import com.vke.api.assets.pipeline.AssetPipelineException;
import com.vke.api.assets.pipeline.StageElement;
import com.vke.api.assets.pipeline.apis.ProtocolResolver;
import com.vke.api.assets.pipeline.stages.PipelineStage;
import com.vke.core.VKEngine;
import com.vke.utils.Identifier;

import java.io.IOException;

public class PipelinedAssetHandle<T> implements AssetHandle<T> {
    private AssetHandle<T> actual;
    private final AssetPipeline pipeline;
    private final Identifier identifier;

    public PipelinedAssetHandle(AssetPipeline pipeline, Identifier identifier) {
        this.pipeline = pipeline;
        this.identifier = identifier;
    }

    @Override
    public Type getType() {
        return actual == null ? Type.Unresolved : actual.getType();
    }

    @Override
    @SuppressWarnings("unchecked")
    public T acquire(VKEngine engine) throws IOException {
        if (actual != null) return actual.acquire(engine);

        try {
            StageElement element = new StageElement(identifier.toPath(), "plain", identifier);
            pipeline.execute(element, PipelineStage.ExecutionTarget.Main);
            String protocol = element.getProtocol();
            ProtocolResolver<?> resolver = pipeline.getContext().getResolver(protocol);
            actual = (AssetHandle<T>) resolver.createHandle(element);
        } catch (AssetPipelineException e) {
            throw new IOException(e);
        }

        return acquire(engine);
    }

    @Override
    public T get() {
        return isAvailable() ? actual.get() : null;
    }

    @Override
    public boolean isAvailable() {
        return actual != null ? actual.isAvailable() : false;
    }

    @Override
    public void free() {
        if (isAvailable()) actual.free();
    }
}
