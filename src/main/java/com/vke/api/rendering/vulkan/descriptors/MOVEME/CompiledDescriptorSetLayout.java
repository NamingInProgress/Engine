package com.vke.api.rendering.vulkan.descriptors.MOVEME;

import com.vke.api.rendering.vulkan.descriptors.info.BindingLayout;
import com.vke.api.rendering.vulkan.descriptors.info.DescriptorSetLayout;
import com.vke.core.VKEngine;
import com.vke.core.vulkan.device.VulkanRenderDevice;
import com.vke.utils.io.Disposable;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK14;
import org.lwjgl.vulkan.VkDescriptorSetLayoutBinding;
import org.lwjgl.vulkan.VkDescriptorSetLayoutCreateInfo;

import java.nio.LongBuffer;

public class CompiledDescriptorSetLayout implements Disposable {

    private final VKEngine engine;
    private final VulkanRenderDevice device;

    private long handle;

    public CompiledDescriptorSetLayout(VKEngine engine, VulkanRenderDevice device, DescriptorSetLayout layout) {
        this.engine = engine;
        this.device = device;

        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkDescriptorSetLayoutBinding.Buffer buf = VkDescriptorSetLayoutBinding.calloc(layout.bindings.size(), stack);

            for (int i = 0; i < layout.bindings.size(); i++) {
                BindingLayout binding = layout.bindings.get(i);
                buf.get(i)
                        .binding(binding.binding)
                        .descriptorCount(binding.descriptorCount)
                        .descriptorType(binding.type.getVkHandle());
            }

            VkDescriptorSetLayoutCreateInfo createInfo = VkDescriptorSetLayoutCreateInfo.calloc(stack)
                    .sType$Default()
                    .pBindings(buf);

            LongBuffer pLayout = stack.mallocLong(1);
            if (VK14.vkCreateDescriptorSetLayout(device.getLogicalDevice().getDevice(), createInfo, null, pLayout) != VK14.VK_SUCCESS) {
                engine.throwException(new IllegalStateException("Failed to create descriptor set layout!"), "CompiledDescriptorSetLayout");
            }

            handle = pLayout.get(0);
        }
    }

    public long getHandle() {
        return handle;
    }

    @Override
    public void free() {
        VK14.vkDestroyDescriptorSetLayout(device.getLogicalDevice().getDevice(), handle, null);
    }
}
