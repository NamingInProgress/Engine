package com.vke.api.rendering.vulkan.descriptors.handles.single;

import com.vke.api.rendering.abstraction.data.Texture;
import com.vke.api.rendering.abstraction.enums.buffer.PackingType;
import com.vke.api.pipeline.DescriptorData;
import com.vke.api.rendering.vulkan.descriptors.DescriptorType;
import com.vke.api.rendering.vulkan.descriptors.bindings.image.ImageBinding;
import com.vke.api.rendering.vulkan.descriptors.handles.UniformHandle;
import com.vke.api.rendering.vulkan.descriptors.handles.array.ImageArrayHandle;

public class ImageHandle extends ImageArrayHandle {

    public final int index; // 0 if it's not in an array

    public ImageHandle(long setHandle, int binding, DescriptorType bindingType, PackingType packingType, ImageBinding imageBinding, int index) {
        super(setHandle, binding, bindingType, packingType, imageBinding);
        this.index = index;
    }

    public void set(Texture texture) {
        this.set(texture, index);
    }

}
