package com.vke.api.rendering.abstraction.renderer.pipeline;

import com.vke.utils.io.Disposable;

public interface Pipeline extends Disposable {

    PipelineLayout layout();

}
