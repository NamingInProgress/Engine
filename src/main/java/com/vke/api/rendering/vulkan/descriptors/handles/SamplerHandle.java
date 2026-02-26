package com.vke.api.rendering.vulkan.descriptors.handles;

import com.vke.api.rendering.abstraction.data.Sampler;
import com.vke.api.rendering.abstraction.data.Texture;
import com.vke.api.rendering.abstraction.enums.buffer.PackingType;
import com.vke.api.pipeline.DescriptorData;

public class SamplerHandle extends UniformHandle {

    public final int index; // -1 if single sampler

    public Texture texture;
    public Sampler sampler;

    public SamplerHandle(long setHandle, int binding, DescriptorData.Binding.Type bindingType, PackingType packingType, int index) {
        super(setHandle, binding, bindingType, packingType);
        this.index = index;
    }

    public void set(Texture texture, Sampler sampler) {
        this.texture = texture;
        this.sampler = sampler;
    }

}
