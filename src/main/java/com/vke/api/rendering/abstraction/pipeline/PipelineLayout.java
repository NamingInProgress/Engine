package com.vke.api.rendering.abstraction.pipeline;

import com.vke.api.pipeline.fucvk.DescriptorData;
import com.vke.utils.Disposable;

public interface PipelineLayout extends Disposable {

    int pushConstantSize();
    int descriptorCount();

}
