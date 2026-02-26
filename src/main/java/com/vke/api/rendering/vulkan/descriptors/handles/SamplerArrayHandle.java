package com.vke.api.rendering.vulkan.descriptors.handles;

import com.vke.api.rendering.abstraction.data.Sampler;
import com.vke.api.rendering.abstraction.data.Texture;
import com.vke.api.rendering.abstraction.enums.buffer.PackingType;
import com.vke.api.pipeline.DescriptorData;

import java.util.ArrayList;
import java.util.List;

public class SamplerArrayHandle extends UniformHandle {

    public final int arraySize;

    public final List<Texture> textures;
    public final List<Sampler> samplers;

    public SamplerArrayHandle(long setHandle, int binding, DescriptorData.Binding.Type bindingType, PackingType packingType, int arraySize) {
        super(setHandle, binding, bindingType, packingType);
        this.arraySize = arraySize;
        this.textures = new ArrayList<>(arraySize);
        this.samplers = new ArrayList<>(arraySize);
    }

    public void set(Texture texture, Sampler sampler, int index) {
        textures.set(index, texture);
        samplers.set(index, sampler);
    }

}
