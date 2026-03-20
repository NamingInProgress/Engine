package com.vke.core.assets.pipeline.stages;

import com.vke.api.parsing.config.node.ConfigArrayNode;
import com.vke.api.parsing.config.node.ConfigNode;
import com.vke.core.assets.pipeline.AssetPipelineException;
import com.vke.core.assets.pipeline.PipelineContext;
import com.vke.core.assets.pipeline.StageElement;
import com.vke.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public abstract class CompoundPipelineStage implements PipelineStage {
    protected final List<PipelineStage> stages;

    public CompoundPipelineStage(ConfigArrayNode node, PipelineContext factories, String... usedFields) throws AssetPipelineException {
        stages = new ArrayList<>();
        ConfigNode[] values = node.values();
        for (ConfigNode value : values) {
            String nodeName = value.getNodeName();
            if (nodeName != null && !Utils.arrayContains(usedFields, nodeName)) {
                PipelineStage stage = factories.produceStage(nodeName, value);
                stages.add(stage);
            }
        }
    }

    protected void processInnerPipeline(StageElement element, ExecutionTarget executionTarget) throws AssetPipelineException {
        for (PipelineStage stage : stages) {
            if (stage.executionTarget().isUsable(executionTarget)) {
                stage.execute(element, executionTarget);
            }
        }
    }
}
