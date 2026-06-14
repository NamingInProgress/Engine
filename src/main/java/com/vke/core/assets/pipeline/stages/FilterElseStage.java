package com.vke.core.assets.pipeline.stages;

import com.vke.api.parsing.config.node.ConfigNode;
import com.vke.core.assets.AssetException;
import com.vke.core.assets.pipeline.PipelineContext;
import com.vke.core.assets.pipeline.StageElement;

public class FilterElseStage extends CompoundPipelineStage{
    public static final String STAGE = "filter-else";
    private final String tag;

    public FilterElseStage(ConfigNode node, PipelineContext factories) throws AssetException {
        super(node.asArray(), factories, "tag");
        this.tag = node.getString("tag");
    }

    @Override
    public void execute(StageElement stageElement, ExecutionTarget executionTarget) throws AssetException {
        processInnerPipeline(stageElement, executionTarget);
    }

    public String getTag() {
        return tag;
    }
}
