package com.vke.api.assets.pipeline;

import com.vke.api.assets.AssetHandle;
import com.vke.api.assets.pipeline.apis.ProtocolResolver;
import com.vke.api.assets.pipeline.stages.CompoundPipelineStage;
import com.vke.api.parsing.config.node.ConfigArrayNode;

public class AssetPipeline extends CompoundPipelineStage {
    private final PipelineContext context;

    public AssetPipeline(ConfigArrayNode node, PipelineContext context) throws AssetPipelineException {
        super(node, context);
        this.context = context;
    }

    @Override
    public void execute(StageElement stageElement) throws AssetPipelineException {
        processInnerPipeline(stageElement);
    }

    public PipelineContext getContext() {
        return context;
    }

    public AssetHandle<?> extractHandle(StageElement stageElement) throws AssetPipelineException {
        String protocol = stageElement.getProtocol();
        ProtocolResolver<?> resolver = context.getResolver(protocol);
        return resolver.createHandle(stageElement);
    }
}
