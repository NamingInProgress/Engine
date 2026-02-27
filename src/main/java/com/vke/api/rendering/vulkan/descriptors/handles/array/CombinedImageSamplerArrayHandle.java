package com.vke.api.rendering.vulkan.descriptors.handles.array;

import com.vke.api.rendering.abstraction.data.Sampler;
import com.vke.api.rendering.abstraction.data.Texture;
import com.vke.api.rendering.abstraction.enums.buffer.PackingType;
import com.vke.api.rendering.vulkan.descriptors.DescriptorType;
import com.vke.api.rendering.vulkan.descriptors.bindings.CombinedImageSamplerBinding;
import com.vke.api.rendering.vulkan.descriptors.handles.UniformHandle;
import com.vke.core.vulkan.descriptor.DescriptorWriter;
import com.vke.core.vulkan.sampler.VulkanSampler;
import com.vke.core.vulkan.texture.VulkanTexture;
import org.jetbrains.annotations.ApiStatus;

public class CombinedImageSamplerArrayHandle extends UniformHandle {

    public final CombinedImageSamplerBinding cisBinding;

    public CombinedImageSamplerArrayHandle(long setHandle, int binding, DescriptorType bindingType, PackingType packingType, CombinedImageSamplerBinding cisBinding) {
        super(setHandle, binding, bindingType, packingType);
        this.cisBinding = cisBinding;
    }

    public void set(Texture texture, Sampler sampler, int index) {
        this.cisBinding.textures[index] = (VulkanTexture) texture;
        this.cisBinding.samplers[index] = (VulkanSampler) sampler;
    }

    @Override
    @ApiStatus.Internal
    public void writeDescriptor(DescriptorWriter writer) {
        writer.writeCombinedImageSamplers(setHandle, binding, cisBinding.textures, cisBinding.samplers);
    }

}
