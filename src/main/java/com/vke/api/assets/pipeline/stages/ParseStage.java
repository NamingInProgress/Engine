package com.vke.api.assets.pipeline.stages;

import com.vke.api.assets.pipeline.PipelineContext;
import com.vke.api.parsing.config.node.ConfigNode;
import com.vke.api.assets.pipeline.AssetPipelineException;
import com.vke.api.assets.pipeline.StageElement;
import com.vke.api.assets.pipeline.apis.AssetParser;

public class ParseStage extends ExecutingStage<AssetParser> {
    public static final String STAGE = "parse";
    private final PipelineContext context;

    public ParseStage(ConfigNode node, PipelineContext context) throws AssetPipelineException {
        super(node, "parser");
        this.context = context;
    }

    @Override
    public void execute(StageElement stageElement, ExecutionTarget executionTarget) throws AssetPipelineException {
        try {
            instance.processStageElement(stageElement, context);
        } catch (AssetPipelineException e) {
            String msg = String.format("There was an exception while using %s -> %s", executorClass, e);
            throw AssetPipelineException.inStage(STAGE, msg);
        }
    }

    @Override
    public ExecutionTarget executionTarget() {
        return ExecutionTarget.Main;
    }

    @Override
    protected String getStageName() {
        return STAGE;
    }
}
