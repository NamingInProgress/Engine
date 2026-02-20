package com.vke.api.abstraction.pipeline;

import com.vke.utils.Disposable;

public interface Pipeline extends Disposable {

    PipelineLayout layout();

}
