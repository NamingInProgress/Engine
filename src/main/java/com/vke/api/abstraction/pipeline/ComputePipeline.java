package com.vke.api.abstraction.pipeline;

import com.vke.utils.Disposable;

public interface ComputePipeline extends Disposable {

    PipelineLayout layout();

}
