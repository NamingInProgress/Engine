package com.vke.api.abstraction.pipeline;

import com.vke.utils.io.Disposable;

public interface GraphicsPipeline extends Disposable {

    PipelineLayout layout();

}
