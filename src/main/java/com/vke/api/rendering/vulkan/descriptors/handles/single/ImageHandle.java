package com.vke.api.rendering.vulkan.descriptors.handles.single;

import com.vke.api.rendering.abstraction.data.Texture;
import com.vke.api.rendering.abstraction.enums.buffer.PackingType;
import com.vke.api.rendering.vulkan.descriptors.DescriptorType;
import com.vke.api.rendering.vulkan.descriptors.bindings.image.ImageBinding;
import com.vke.api.rendering.vulkan.descriptors.handles.UniformHandle;
import com.vke.api.rendering.vulkan.descriptors.handles.array.ImageArrayHandle;
import com.vke.core.vulkan.descriptor.CompiledDescriptorSetLayout;

public class ImageHandle extends ImageArrayHandle {

    public final int index; // 0 if it's not in an array

    public ImageHandle(int descriptorSetListIndex, int binding, DescriptorType bindingType, PackingType packingType, CompiledDescriptorSetLayout compiledLayout, ImageBinding imageBinding, int index) {
        super(descriptorSetListIndex, binding, bindingType, packingType, compiledLayout, imageBinding);
        this.index = index;
    }

    public void set(Texture texture) {
        this.set(texture, index);
    }

    @Override
    public <T extends UniformHandle> T copy() {
        return (T) new ImageHandle(descriptorSetListIndex, binding, bindingType, packingType, compiledLayout, imageBinding.copy(), index);
    }
}
