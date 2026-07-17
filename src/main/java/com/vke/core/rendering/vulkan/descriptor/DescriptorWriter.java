package com.vke.core.rendering.vulkan.descriptor;

import com.vke.api.rendering.vulkan.descriptors.DescriptorType;
import com.vke.core.memory.AutoHeapAllocator;
import com.vke.core.rendering.vulkan.device.VulkanRenderDevice;
import com.vke.core.rendering.vulkan.sampler.VulkanSampler;
import com.vke.core.rendering.vulkan.service.VulkanRenderSystem;
import com.vke.core.rendering.vulkan.texture.VulkanImageView;
import com.vke.utils.io.Disposable;
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

    public DescriptorWriter(VulkanRenderSystem ctx) {
        this.device = ctx.device();
        this.alloc = new AutoHeapAllocator();
    }

    public void writeBuffer(long setHandle, int binding, long size, long offset, long gpuBuffer, DescriptorType type) {
        writeBuffer(setHandle, binding, size, offset, gpuBuffer, 0, type);
    }

    public void writeBuffer(long setHandle, int binding, long size, long offset, long gpuBuffer, int arrayIndex, DescriptorType type) {
        VkDescriptorBufferInfo.Buffer bufferInfos = alloc.allocBuffer(VkDescriptorBufferInfo.SIZEOF, 1, VkDescriptorBufferInfo.Buffer::new);
        bufferInfos.get(0)
                .offset(offset)
                .range(size)
                .buffer(gpuBuffer);
        
        writeData.add(new WriteData(bufferInfos, setHandle, binding, type, 1, arrayIndex));
    }

    public void writeBufferArray(long setHandle, int binding, long bufferSize, int bufferCount, long gpuBuffer, DescriptorType type) {
        for (int i = 0; i < bufferCount; i++) {
            writeBuffer(setHandle, binding, bufferSize, i * bufferSize, gpuBuffer, type);
        }
    }

    /// [ ----- COMBINED IMAGE SAMPLERS ----- ]

    public void writeCombinedImageSampler(long setHandle, int binding, VulkanImageView view, VulkanSampler sampler) {
        writeCombinedImageSampler(setHandle, binding, view, sampler, 0);
    }

    public void writeCombinedImageSampler(long setHandle, int binding, VulkanImageView view, VulkanSampler sampler, int arrayIndex) {
        VkDescriptorImageInfo.Buffer imageInfos = alloc.allocBuffer(VkDescriptorImageInfo.SIZEOF, 1, VkDescriptorImageInfo.Buffer::new);
        imageInfos.get(0)
                .imageView(view.getHandle())
                .sampler(sampler.getHandle())
                .imageLayout(view.parent().getState().getLayoutHandle());

        writeData.add(new WriteData(imageInfos, setHandle, binding, DescriptorType.COMBINED_IMAGE_SAMPLER, 1, arrayIndex));
    }

    public void writeCombinedImageSamplers(long setHandle, int binding, VulkanImageView[] views, VulkanSampler[] samplers) {
        if (views.length != samplers.length) throw new IllegalStateException("Textures and Samplers array sizes do not match!");

        for (int i = 0; i < samplers.length; i++) {
            VulkanImageView tex = views[i];
            VulkanSampler sampler = samplers[i];

            if (tex == null || sampler == null) continue;

            writeCombinedImageSampler(setHandle, binding, tex, sampler, i);
        }
    }

    /// [ ----- SAMPLERS ----- ]

    public void writeSampler(long setHandle, int binding, VulkanSampler sampler) {
        writeSampler(setHandle, binding, sampler, 0);
    }

    public void writeSampler(long setHandle, int binding, VulkanSampler sampler, int arrayIndex) {
        VkDescriptorImageInfo.Buffer samplerInfos = alloc.allocBuffer(VkDescriptorImageInfo.SIZEOF, 1, VkDescriptorImageInfo.Buffer::new);
        samplerInfos.get(0)
                .sampler(sampler.getHandle());

        writeData.add(new WriteData(samplerInfos, setHandle, binding, DescriptorType.SAMPLER, 1, arrayIndex));
    }

    public void writeSamplers(long setHandle, int binding, VulkanSampler[] samplers) {
        for (int i = 0; i < samplers.length; i++) {
            if (samplers[i] == null) continue;

            writeSampler(setHandle, binding, samplers[i], i);
        }
    }

    /// [ ----- IMAGES ----- ]

    public void writeImage(long setHandle, int binding, VulkanImageView view, DescriptorType type) {
        writeImage(setHandle, binding, view, type, 0);
    }

    public void writeImage(long setHandle, int binding, VulkanImageView view, DescriptorType type, int arrayIndex) {
        VkDescriptorImageInfo.Buffer imageInfos = alloc.allocBuffer(VkDescriptorImageInfo.SIZEOF, 1, VkDescriptorImageInfo.Buffer::new);
        imageInfos.get(0)
                .imageView(view.getHandle())
                .sampler(VK14.VK_NULL_HANDLE)
                .imageLayout(view.parent().getState().getLayoutHandle());

        writeData.add(new WriteData(imageInfos, setHandle, binding, type, 1, arrayIndex));
    }

    public void writeImages(long setHandle, int binding, VulkanImageView[] views, DescriptorType type) {
        for (int i = 0; i < views.length; i++) {
            if (views[i] == null) continue;
            writeImage(setHandle, binding, views[i], type, i);
        }
    }

    public void flush() {
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

            VK14.vkUpdateDescriptorSets(device.vkLogicalDevice(), buf, null);
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

    public record WriteData(StructBuffer<?, ?> writeData, long setHandle, int binding, DescriptorType bindingType,
                            int descriptorCount, int dstArrayElement) { }

}
