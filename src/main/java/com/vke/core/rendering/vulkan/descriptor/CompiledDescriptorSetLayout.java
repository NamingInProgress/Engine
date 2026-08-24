package com.vke.core.rendering.vulkan.descriptor;

import com.vke.api.rendering.vulkan.descriptors.info.BindingLayout;
import com.vke.api.rendering.vulkan.descriptors.info.DescriptorSetLayout;
import com.vke.api.rendering.vulkan.descriptors.info.DescriptorsInfo;
import com.vke.core.rendering.vulkan.service.VulkanRenderSystem;
import com.vke.utils.io.Disposable;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK14;
import org.lwjgl.vulkan.VkDescriptorSetLayoutBinding;
import org.lwjgl.vulkan.VkDescriptorSetLayoutBindingFlagsCreateInfo;
import org.lwjgl.vulkan.VkDescriptorSetLayoutCreateInfo;

import java.nio.IntBuffer;
import java.nio.LongBuffer;

public class CompiledDescriptorSetLayout implements Disposable {

    private final DescriptorSetLayout layout;
    private final DescriptorsInfo additionalInfo;
    private final VulkanRenderSystem ctx;

    private long handle;

    public CompiledDescriptorSetLayout(VulkanRenderSystem ctx, DescriptorSetLayout layout, DescriptorsInfo additionalInfo) {
        this(ctx, layout, additionalInfo, false);
    }

    public CompiledDescriptorSetLayout(VulkanRenderSystem ctx, DescriptorSetLayout layout, DescriptorsInfo additionalInfo, boolean partialBinding) {
        this.layout = layout;
        this.additionalInfo = additionalInfo;
        this.ctx = ctx;

        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkDescriptorSetLayoutBinding.Buffer buf = null;

            if (layout.bindings != null && !layout.bindings.isEmpty()) {
                buf = VkDescriptorSetLayoutBinding.calloc(layout.bindings.size(), stack);

                int i = 0;
                for (BindingLayout binding : layout.bindings) {
                    buf.get(i++)
                            .binding(binding.binding)
                            .descriptorCount(binding.descriptorCount)
                            .descriptorType(binding.type.getVkHandle())
                            .stageFlags(VK14.VK_SHADER_STAGE_ALL);
                }
            }

            VkDescriptorSetLayoutCreateInfo createInfo = VkDescriptorSetLayoutCreateInfo.calloc(stack)
                    .sType$Default()
                    .pBindings(buf);

            if (partialBinding) {
                IntBuffer flags = stack.mallocInt(1);
                flags.put(0, VK14.VK_DESCRIPTOR_BINDING_PARTIALLY_BOUND_BIT |
                                    VK14.VK_DESCRIPTOR_BINDING_VARIABLE_DESCRIPTOR_COUNT_BIT |
                                    VK14.VK_DESCRIPTOR_BINDING_UPDATE_AFTER_BIND_BIT);
                VkDescriptorSetLayoutBindingFlagsCreateInfo flagsInfo = VkDescriptorSetLayoutBindingFlagsCreateInfo.calloc(stack)
                        .sType$Default()
                        .bindingCount(1)
                        .pBindingFlags(flags);

                createInfo.pNext(flagsInfo);
            }

            LongBuffer pLayout = stack.mallocLong(1);
            if (VK14.vkCreateDescriptorSetLayout(ctx.device().vkLogicalDevice(), createInfo, null, pLayout) != VK14.VK_SUCCESS) {
                ctx.throwException(new IllegalStateException("Failed to create descriptor set layout!"), "CompiledDescriptorSetLayout");
            }

            handle = pLayout.get(0);
        }
    }

    public long getHandle() {
        return handle;
    }

    public DescriptorSetLayout getLayout() {
        return layout;
    }

    public DescriptorsInfo getAdditionalInfo() {
        return additionalInfo;
    }

    @Override
    public void free() {
        if (handle != 0) {
            VK14.vkDestroyDescriptorSetLayout(ctx.device().vkLogicalDevice(), handle, null);
            handle = 0;
        }
    }
}
