package com.vke.core.assets.pipeline.apis;

import com.vke.core.assets.pipeline.AssetPipelineException;
import com.vke.core.assets.pipeline.PipelineContext;
import com.vke.core.assets.pipeline.StageElement;

public interface AssetValidator<ERROR extends Throwable> {
    ERROR processStageElement(StageElement stageElement, PipelineContext context) throws AssetPipelineException;
}
