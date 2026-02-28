package com.vke.api.assets.pipeline.stages;

import com.vke.api.assets.pipeline.PipelineContext;
import com.vke.api.parsing.config.node.ConfigNode;
import com.vke.api.assets.pipeline.AssetPipelineException;
import com.vke.api.assets.pipeline.StageElement;
import com.vke.api.assets.pipeline.apis.AssetValidator;

public class ValidateStage extends ExecutingStage<AssetValidator<? extends Throwable>> {
    public static final String STAGE = "validate";

    private final PipelineContext context;

    public ValidateStage(ConfigNode node, PipelineContext context) throws AssetPipelineException {
        super(node, "validator");
        this.context = context;
    }

    @Override
    protected String getStageName() {
        return STAGE;
    }

    @Override
    public void execute(StageElement stageElement) throws AssetPipelineException {
        try {
            Throwable error = instance.processStageElement(stageElement, context);
            if (error != null) {
                String msg = String.format("Validation error for file %s -> %s", stageElement.getPath(), error);
                throw AssetPipelineException.inStage(STAGE, msg);
            }
        } catch (AssetPipelineException e) {
            String msg = String.format("There was an exception while using %s -> %s", executorClass, e);
            throw AssetPipelineException.inStage(STAGE, msg);
        }
    }
}
