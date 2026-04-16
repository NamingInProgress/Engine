package com.vke.api.rendering.vulkan.descriptors.handles.array;

import com.vke.api.rendering.abstraction.data.Texture;
import com.vke.api.rendering.abstraction.enums.buffer.PackingType;
import com.vke.api.rendering.vulkan.descriptors.DescriptorType;
import com.vke.api.rendering.vulkan.descriptors.bindings.image.ImageBinding;
import com.vke.api.rendering.vulkan.descriptors.handles.UniformHandle;
import com.vke.core.vulkan.descriptor.CompiledDescriptorSetLayout;
import com.vke.core.vulkan.descriptor.DescriptorWriter;
import com.vke.core.vulkan.texture.VulkanTexture;

public class ImageArrayHandle extends UniformHandle {

    public final ImageBinding imageBinding;

    public ImageArrayHandle(int descriptorSetListIndex, int binding, DescriptorType bindingType, PackingType packingType, CompiledDescriptorSetLayout compiledLayout, ImageBinding imageBinding) {
        super(descriptorSetListIndex, binding, bindingType, packingType, compiledLayout);
        this.imageBinding = imageBinding;
    }

    public void set(Texture texture, int index) {
        this.imageBinding.textures[index] = (VulkanTexture) texture;
    }

    @Override
    public void writeDescriptor(DescriptorWriter writer, long handle) {
        writer.writeImages(handle, binding, imageBinding.textures, bindingType);
    }

    @Override
    public <T extends UniformHandle> T copy() {
        return (T) new ImageArrayHandle(descriptorSetListIndex, binding, bindingType, packingType, compiledLayout, imageBinding.copy());
    }
}
