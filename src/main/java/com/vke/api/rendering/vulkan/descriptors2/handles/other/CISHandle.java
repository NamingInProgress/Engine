package com.vke.api.rendering.vulkan.descriptors2.handles.other;

import com.vke.api.rendering.abstraction.data.Sampler;
import com.vke.api.rendering.abstraction.data.Texture;
import com.vke.api.rendering.vulkan.descriptors.DescriptorType;
import com.vke.api.rendering.vulkan.descriptors.bindings.CombinedImageSamplerBinding;
import com.vke.api.rendering.vulkan.descriptors2.handles.UniformHandle;
import com.vke.core.vulkan.descriptor.DescriptorWriter;
import com.vke.core.vulkan.descriptor.ds2.DescriptorSetInstance;
import com.vke.core.vulkan.sampler.VulkanSampler;
import com.vke.core.vulkan.texture.VulkanTexture;
import org.jetbrains.annotations.ApiStatus;

public class CISHandle extends UniformHandle {

    private final CombinedImageSamplerBinding cisBinding;

    public CISHandle(DescriptorSetInstance instance, int set, int binding, DescriptorType type, CombinedImageSamplerBinding cisBinding) {
        super(instance, set, binding, type);
        this.cisBinding = cisBinding;
    }

    public void set(Texture texture, Sampler sampler) {
        this.cisBinding.textures[0] = (VulkanTexture) texture;
        this.cisBinding.samplers[0] = (VulkanSampler) sampler;
        instance.cacheCISWrite(binding, cisBinding.textures, cisBinding.samplers);
    }

    @ApiStatus.Internal
    public void writeDescriptor(DescriptorWriter writer, long handle) {
        writer.writeCombinedImageSamplers(handle, binding, cisBinding.textures, cisBinding.samplers);
    }

}