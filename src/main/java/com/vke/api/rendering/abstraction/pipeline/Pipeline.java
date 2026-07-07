package com.vke.api.rendering.abstraction.pipeline;

import com.vke.api.rendering.vulkan.descriptors2.handles.UniformHandle;
import com.vke.utils.io.Disposable;

public interface Pipeline extends Disposable {

    PipelineLayout layout();

}
