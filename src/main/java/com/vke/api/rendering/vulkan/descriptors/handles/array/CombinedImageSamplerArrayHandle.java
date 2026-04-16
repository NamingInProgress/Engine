package com.vke.api.rendering.vulkan.descriptors.handles.array;

import com.vke.api.rendering.abstraction.data.Sampler;
import com.vke.api.rendering.abstraction.data.Texture;
import com.vke.api.rendering.abstraction.enums.buffer.PackingType;
import com.vke.api.rendering.vulkan.descriptors.DescriptorType;
import com.vke.api.rendering.vulkan.descriptors.bindings.CombinedImageSamplerBinding;
import com.vke.api.rendering.vulkan.descriptors.handles.UniformHandle;
import com.vke.core.vulkan.descriptor.CompiledDescriptorSetLayout;
import com.vke.core.vulkan.descriptor.DescriptorWriter;
import com.vke.core.vulkan.sampler.VulkanSampler;
import com.vke.core.vulkan.texture.VulkanTexture;
import org.jetbrains.annotations.ApiStatus;

public class CombinedImageSamplerArrayHandle extends UniformHandle {

    public final CombinedImageSamplerBinding cisBinding;

    public CombinedImageSamplerArrayHandle(int descriptorSetListIndex, int binding, DescriptorType bindingType, PackingType packingType, CompiledDescriptorSetLayout compiledLayout, CombinedImageSamplerBinding cisBinding) {
        super(descriptorSetListIndex, binding, bindingType, packingType, compiledLayout);
        this.cisBinding = cisBinding;
    }

    public void set(Texture texture, Sampler sampler, int index) {
        this.cisBinding.textures[index] = (VulkanTexture) texture;
        this.cisBinding.samplers[index] = (VulkanSampler) sampler;
    }

    @Override
    @ApiStatus.Internal
    public void writeDescriptor(DescriptorWriter writer, long handle) {
        writer.writeCombinedImageSamplers(handle, binding, cisBinding.textures, cisBinding.samplers);
    }

    @Override
    public <T extends UniformHandle> T copy() {
        return (T) new CombinedImageSamplerArrayHandle(descriptorSetListIndex, binding, bindingType, packingType, compiledLayout, cisBinding.copy());
    }

}
