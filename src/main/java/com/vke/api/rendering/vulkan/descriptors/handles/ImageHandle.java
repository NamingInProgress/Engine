package com.vke.api.rendering.vulkan.descriptors.handles;

import com.vke.api.rendering.abstraction.data.Texture;
import com.vke.api.rendering.abstraction.enums.buffer.PackingType;
import com.vke.api.pipeline.DescriptorData;

public class ImageHandle extends UniformHandle {

    public final int index; // -1 if it's not in an array

    public Texture texture;

    public ImageHandle(long setHandle, int binding, DescriptorData.Binding.Type bindingType, PackingType packingType, int index) {
        super(setHandle, binding, bindingType, packingType);
        this.index = index;
    }

    public void set(Texture texture) {
        this.texture = texture;
    }

}
