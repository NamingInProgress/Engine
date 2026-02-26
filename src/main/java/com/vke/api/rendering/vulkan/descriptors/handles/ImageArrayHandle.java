package com.vke.api.rendering.vulkan.descriptors.handles;

import com.vke.api.rendering.abstraction.data.Texture;
import com.vke.api.rendering.abstraction.enums.buffer.PackingType;
import com.vke.api.pipeline.DescriptorData;

import java.util.ArrayList;
import java.util.List;

public class ImageArrayHandle extends UniformHandle {

    public final int arraySize;

    public final List<Texture> textures;

    public ImageArrayHandle(long setHandle, int binding, DescriptorData.Binding.Type bindingType, PackingType packingType, int arraySize) {
        super(setHandle, binding, bindingType, packingType);
        this.arraySize = arraySize;
        this.textures = new ArrayList<>(arraySize);
    }

    public void set(Texture texture, int index) {
        textures.set(index, texture);
    }

}
