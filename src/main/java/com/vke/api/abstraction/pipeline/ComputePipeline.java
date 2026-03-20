package com.vke.api.abstraction.pipeline;

import com.vke.utils.io.Disposable;

public interface ComputePipeline extends Disposable {

    PipelineLayout layout();

}
