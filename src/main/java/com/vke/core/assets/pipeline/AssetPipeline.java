package com.vke.core.assets.pipeline;

import com.vke.api.assets.AssetHandle;
import com.vke.api.assets.Protocols;
import com.vke.core.assets.pipeline.apis.AssetProtocol;
import com.vke.core.assets.pipeline.protocols.loader.PipelinedLoader;
import com.vke.core.assets.pipeline.stages.CompoundPipelineStage;
import com.vke.api.parsing.config.node.ConfigArrayNode;

public class AssetPipeline extends CompoundPipelineStage {
    private final PipelineContext context;

    public AssetPipeline(ConfigArrayNode node, PipelineContext context) throws AssetPipelineException {
        super(node, context);
        this.context = context;
    }

    @Override
    public void execute(StageElement stageElement, ExecutionTarget executionTarget) throws AssetPipelineException {
        processInnerPipeline(stageElement, executionTarget);
    }

    public PipelineContext getContext() {
        return context;
    }

    public AssetHandle<?> extractHandle(StageElement stageElement) throws AssetPipelineException {
        //assets always start as PLAIN
        String protocolName = Protocols.PLAIN;
        AssetProtocol<?> protocol = context.getProtocol(protocolName);
        return protocol.createAssetHandle(stageElement.getAssetData(), new PipelinedLoader(this, protocolName));
    }
}
