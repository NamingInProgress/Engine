package com.vke.core.assets.pipeline.stages;

import com.vke.core.assets.pipeline.AssetPipelineException;
import com.vke.core.assets.pipeline.StageElement;

public interface PipelineStage {
    void execute(StageElement stageElement, ExecutionTarget executionTarget) throws AssetPipelineException;

    default ExecutionTarget executionTarget() {
        return ExecutionTarget.All;
    }

    enum ExecutionTarget {
        All,
        Main,
        Pseudo;

        public boolean isUsable(ExecutionTarget target) {
            return this == ExecutionTarget.All || target == this;
        }
    }
}
