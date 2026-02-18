package com.vke.api.abstraction.pipeline;

import com.vke.api.vulkan.descriptors.DescriptorData;
import com.vke.utils.Disposable;

public interface PipelineLayout extends Disposable {

    DescriptorData getDescriptors();

    int pushConstantSize();
    int descriptorCount();

}
