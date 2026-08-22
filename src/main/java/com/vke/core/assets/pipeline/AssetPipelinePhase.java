package com.vke.core.assets.pipeline;

import com.vke.api.assets.AssetHandle;
import com.vke.api.assets.AssetMeta;
import com.vke.api.assets.Protocols;
import com.vke.core.Identifier;
import com.vke.core.assets.AssetException;
import com.vke.core.assets.meta.AssetMetaAttributes;
import com.vke.core.assets.meta.FullAssetMeta;
import com.vke.core.assets.pipeline.apis.AssetProtocol;
import com.vke.core.assets.pipeline.protocols.loader.PipelinedLoader;
import com.vke.core.assets.pipeline.stages.CompoundPipelineStage;
import com.vke.api.parsing.config.node.ConfigArrayNode;

public class AssetPipelinePhase extends CompoundPipelineStage {
    private final String name;
    private final PipelineContext context;

    public AssetPipelinePhase(String phaseName, ConfigArrayNode node, PipelineContext context) throws AssetException {
        super(node, context);
        this.name = phaseName;
        this.context = context;
    }

    @Override
    public void execute(StageElement stageElement, ExecutionTarget executionTarget) throws AssetException {
        processInnerPipeline(stageElement, executionTarget);
    }

    public PipelineContext getContext() {
        return context;
    }

    public AssetHandle<?> extractHandle(StageElement stageElement, AssetMetaAttributes attributes) throws AssetException {
        //assets always start as PLAIN
        String protocolName = Protocols.PLAIN;
        AssetProtocol<?> protocol = context.getProtocol(protocolName);
        Identifier assetName = stageElement.getAssetName();
        AssetMeta meta = new FullAssetMeta(protocolName, stageElement.getBundleName(), assetName, attributes);
        Identifier overrideName = attributes.getOverrideName();
        if (overrideName != null) {
            assetName = overrideName;
        }
        return protocol.createAssetHandle(stageElement, assetName, new PipelinedLoader(this, meta));
    }

    public String getName() {
        return name;
    }
}
