package com.vke.core.assets.pipeline;

import com.vke.api.assets.AssetHandle;
import com.vke.api.assets.Protocols;
import com.vke.core.assets.AssetException;
import com.vke.core.assets.pipeline.apis.AssetProtocol;
import com.vke.core.assets.pipeline.protocols.loader.PipelinedLoader;
import com.vke.core.assets.pipeline.stages.CompoundPipelineStage;
import com.vke.api.parsing.config.node.ConfigArrayNode;
import com.vke.core.logger.LoggerFactory;
import com.vke.utils.io.Identifier;

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

    public AssetHandle<?> extractHandle(StageElement stageElement) throws AssetException {
        //assets always start as PLAIN
        String protocolName = Protocols.PLAIN;
        AssetProtocol<?> protocol = context.getProtocol(protocolName);
        Identifier assetName = stageElement.getAssetName();
        String rawAssetName = assetName.getPath();
        return protocol.createAssetHandle(stageElement.getAssetData(), assetName, new PipelinedLoader(this, protocolName, rawAssetName));

        //TODO for tmr:
        //also implement the <include> tag in asset pipeline to split stuff. when parsing a PipelineStage, just parse a whole new file instead and put the shit in. maybe even make IncludeStage its own thing idk

        //now where the filter-else and phase thingy works, make the schemas load in a separate phase before and perform optional validation in the
        //plain->config converter

    }

    public String getName() {
        return name;
    }
}
