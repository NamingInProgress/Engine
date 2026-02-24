package com.vke.core.vulkan.descriptor;

import com.vke.api.pipeline.DescriptorData;
import com.vke.core.memory.AutoHeapAllocator;
import com.vke.core.vulkan.device.VulkanRenderDevice;
import com.vke.core.vulkan.sampler.VulkanSampler;
import com.vke.core.vulkan.texture.VulkanTexture;
import com.vke.utils.Disposable;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.StructBuffer;
import org.lwjgl.vulkan.VK14;
import org.lwjgl.vulkan.VkDescriptorBufferInfo;
import org.lwjgl.vulkan.VkDescriptorImageInfo;
import org.lwjgl.vulkan.VkWriteDescriptorSet;

import java.util.ArrayList;
import java.util.List;

public class DescriptorWriter implements Disposable {

    private final List<WriteData> writeData = new ArrayList<>();

    private final VulkanRenderDevice device;
    private AutoHeapAllocator alloc;

    public DescriptorWriter(VulkanRenderDevice device) {
        this.device = device;
        this.alloc = new AutoHeapAllocator();
    }

    public void writeBuffer(long setHandle, int binding, int size, long offset, long gpuBuffer, DescriptorData.Binding.Type type) {
        VkDescriptorBufferInfo.Buffer bufferInfos = alloc.allocBuffer(VkDescriptorBufferInfo.SIZEOF, 1, VkDescriptorBufferInfo.Buffer::new);
        bufferInfos.get(0)
                .offset(offset)
                .range(size)
                .buffer(gpuBuffer);
        
        writeData.add(new WriteData(bufferInfos, setHandle, binding, type, 1, 0));
    }

    /// [ ----- SAMPLERS ----- ]

    public void writeSampler(long setHandle, int binding, VulkanTexture texture, VulkanSampler sampler) {
        writeSampler(setHandle, binding, texture, sampler, 0);
    }

    public void writeSampler(long setHandle, int binding, VulkanTexture texture, VulkanSampler sampler, int arrayIndex) {
        VkDescriptorImageInfo.Buffer imageInfos = alloc.allocBuffer(VkDescriptorImageInfo.SIZEOF, 1, VkDescriptorImageInfo.Buffer::new);
        imageInfos.get(0)
                .imageView(texture.getView().getHandle())
                .sampler(sampler.getHandle())
                .imageLayout(texture.getImage().layout().getVkHandle());

        writeData.add(new WriteData(imageInfos, setHandle, binding, DescriptorData.Binding.Type.COMBINED_IMAGE_SAMPLER, 1, arrayIndex));
    }

    public void writeSamplers(long setHandle, int binding, VulkanTexture[] textures, VulkanSampler[] samplers) {
        if (textures.length != samplers.length) throw new IllegalStateException("Textures and Samplers array sizes do not match!");

        for (int i = 0; i < samplers.length; i++) {
            VulkanTexture tex = textures[i];
            VulkanSampler sampler = samplers[i];

            if (tex == null || sampler == null) continue;

            writeSampler(setHandle, binding, tex, sampler, i);
        }
    }

    /// [ ----- IMAGES ----- ]

    public void writeImage(long setHandle, int binding, VulkanTexture texture) {
        writeImage(setHandle, binding, texture, 0);
    }

    public void writeImage(long setHandle, int binding, VulkanTexture texture, int arrayIndex) {
        VkDescriptorImageInfo.Buffer imageInfos = alloc.allocBuffer(VkDescriptorImageInfo.SIZEOF, 1, VkDescriptorImageInfo.Buffer::new);
        imageInfos.get(0)
                .imageView(texture.getView().getHandle())
                .sampler(VK14.VK_NULL_HANDLE)
                .imageLayout(texture.getImage().layout().getVkHandle());

        writeData.add(new WriteData(imageInfos, setHandle, binding, DescriptorData.Binding.Type.COMBINED_IMAGE_SAMPLER, 1, arrayIndex));
    }

    public void writeImages(long setHandle, int binding, VulkanTexture[] textures) {
        for (int i = 0; i < textures.length; i++) {
            if (textures[i] == null) continue;
            writeImage(setHandle, binding, textures[i], i);
        }
    }

    public void writeAll() {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkWriteDescriptorSet.Buffer buf = VkWriteDescriptorSet.calloc(writeData.size(), stack);

            for (int i = 0; i < writeData.size(); i++) {
                WriteData datum = writeData.get(i);
                buf.get(i)
                        .sType$Default()
                        .dstSet(datum.setHandle)
                        .dstBinding(datum.binding)
                        .descriptorCount(datum.descriptorCount)
                        .dstArrayElement(datum.dstArrayElement)
                        .descriptorType(datum.bindingType.getVkHandle());

                if (datum.writeData instanceof VkDescriptorBufferInfo.Buffer) {
                    buf.get(i).pBufferInfo((VkDescriptorBufferInfo.Buffer) datum.writeData);
                } else {
                    buf.get(i).pImageInfo((VkDescriptorImageInfo.Buffer) datum.writeData);
                }
            }

            VK14.vkUpdateDescriptorSets(this.device.getLogicalDevice().getDevice(), buf, null);
            clear();
        }
    }
    
    public void clear() {
        alloc.close();
        alloc = new AutoHeapAllocator();
        this.writeData.clear();
    }

    @Override
    public void free() {
        alloc.close();
        this.writeData.clear();
    }

    public record WriteData(StructBuffer<?, ?> writeData, long setHandle, int binding, DescriptorData.Binding.Type bindingType,
                            int descriptorCount, int dstArrayElement) { }

}
