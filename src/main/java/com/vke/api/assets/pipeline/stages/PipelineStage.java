package com.vke.api.assets.pipeline.stages;

import com.vke.api.assets.pipeline.AssetPipelineException;
import com.vke.api.assets.pipeline.StageElement;

public interface PipelineStage {
    void execute(StageElement stageElement) throws AssetPipelineException;
}
