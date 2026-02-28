package com.vke.api.assets.pipeline.apis;

import com.vke.api.assets.pipeline.AssetPipelineException;
import com.vke.api.assets.pipeline.PipelineContext;
import com.vke.api.assets.pipeline.StageElement;

public interface AssetValidator<ERROR extends Throwable> {
    ERROR processStageElement(StageElement stageElement, PipelineContext context) throws AssetPipelineException;
}
