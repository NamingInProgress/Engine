package com.vke.core.assets.pipeline.stages;

import com.vke.api.parsing.config.node.ConfigNode;
import com.vke.core.assets.AssetException;
import com.vke.core.assets.pipeline.PipelineContext;
import com.vke.core.assets.pipeline.StageElement;
import com.vke.core.assets.pipeline.apis.AssetData;
import com.vke.core.assets.pipeline.apis.AssetProcessor;
import com.vke.core.assets.pipeline.apis.AssetProtocol;

public class ProcessStage extends ParameterizedStage {
    public static final String STAGE = "process";
    private final PipelineContext context;
    private final String processorName;

    public ProcessStage(ConfigNode node, PipelineContext context) {
        super(node);
        this.context = context;
        this.processorName = node.getString("using");
    }

    @Override
    public void execute(StageElement stageElement, ExecutionTarget executionTarget) throws AssetException {
        AssetProcessor processor = context.getProcessor(processorName);
        if (processor == null) {
            throw new AssetException("No processor with the name '" + processorName + "' registered!");
        }

        if (executionTarget != ExecutionTarget.Pseudo) {
            AssetProtocol<?> protocol = context.getProtocol(stageElement.getProtocol());
            AssetData data = stageElement.getAssetDataResolved(context, protocol, executionTarget);
            processor.process(context, stageElement, data, arguments);
        }
    }
}
