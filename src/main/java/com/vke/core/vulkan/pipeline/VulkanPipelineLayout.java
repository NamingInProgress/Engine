package com.vke.core.vulkan.pipeline;

import com.carrotsearch.hppc.ObjectIntHashMap;
import com.vke.api.rendering.FrameCounter;
import com.vke.api.rendering.abstraction.pipeline.PipelineLayout;
import com.vke.api.rendering.vulkan.descriptors.DescriptorSets;
import com.vke.api.rendering.vulkan.descriptors.DescriptorType;
import com.vke.api.rendering.vulkan.descriptors.info.DescriptorSetLayout;
import com.vke.core.vulkan.descriptor.CompiledDescriptorSetLayout;
import com.vke.api.rendering.vulkan.pushconstants.PushConstants;
import com.vke.core.VKEngine;
import com.vke.core.vulkan.descriptor.DescriptorAllocator;
import com.vke.core.vulkan.descriptor.ds2.DescriptorSetInstance;
import com.vke.core.vulkan.device.VulkanRenderDevice;
import com.vke.utils.Utils;
import com.vke.utils.io.Disposable;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK14;
import org.lwjgl.vulkan.VkPipelineLayoutCreateInfo;
import org.lwjgl.vulkan.VkPushConstantRange;

import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class VulkanPipelineLayout implements PipelineLayout {

    public static final HashMap<LayoutCapabilities, VulkanPipelineLayout> LAYOUT_CACHE = new HashMap<>();

    private final long handle;

    private final VulkanRenderDevice device;
    private final VKEngine engine;

    private final PushConstants pushConstants;

    private final DescriptorAllocator alloc;
    private final List<DescriptorSetInstance> userSets = new ArrayList<>();

    public static VulkanPipelineLayout getLayout(VKEngine engine, VulkanRenderDevice device, PushConstants pc, List<DescriptorSetLayout> layouts) {
        // TODO: Fix this making a new pipeline layout (This is technically fine but it is recommended to reuse)
        FrameCounter fc = engine.service(engine.rendererType().serviceName);
        if (Utils.TRUE) return new VulkanPipelineLayout(engine, device, fc, pc, layouts);

        LayoutCapabilities cap = new LayoutCapabilities();
        if (LAYOUT_CACHE.containsKey(cap)) return LAYOUT_CACHE.get(cap);
        LAYOUT_CACHE.put(cap, new VulkanPipelineLayout(engine, device, fc, pc, layouts));
        return LAYOUT_CACHE.get(cap);
    }

    private VulkanPipelineLayout(VKEngine engine, VulkanRenderDevice device, FrameCounter fc, PushConstants pc, List<DescriptorSetLayout> layouts) {
        this.engine = engine;
        this.device = device;
        this.pushConstants = pc;

        ObjectIntHashMap<DescriptorType> counts = new ObjectIntHashMap<>();
        layouts.forEach(setLayout -> setLayout.bindings.forEach(bindingLayout ->
                counts.addTo(bindingLayout.type, bindingLayout.descriptorCount)));

        this.alloc = new DescriptorAllocator(engine, device, counts, fc.framesInFlight(), fc.framesInFlight(), false);

        for (int i = 0; i < layouts.size(); i++) {
            DescriptorSetLayout layout = layouts.get(i);
            userSets.add(new DescriptorSetInstance(engine, device, alloc, layout, fc, i));
        }

        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkPipelineLayoutCreateInfo createInfo = VkPipelineLayoutCreateInfo.calloc(stack)
                    .sType$Default();

            VkPushConstantRange.Buffer pushConstantsBuffer = null;
            if (pc.getLayout().size != 0) {
                pushConstantsBuffer = VkPushConstantRange.calloc(1, stack);
                pushConstantsBuffer.get(0)
                        .offset(0)
                        .size((int) pc.getLayout().size)
                        .stageFlags(VK14.VK_SHADER_STAGE_ALL);
            }

            LongBuffer pDescriptors = stack.longs(userSets.stream().mapToLong(set -> set.getCompiledLayout().getHandle()).toArray());

            createInfo.pSetLayouts(pDescriptors);
            createInfo.setLayoutCount(userSets.size());
            createInfo.pPushConstantRanges(pushConstantsBuffer);

            LongBuffer pLayout = stack.mallocLong(1);
            if (VK14.vkCreatePipelineLayout(device.getLogicalDevice().getDevice(), createInfo, null, pLayout) != VK14.VK_SUCCESS) {
                engine.throwException(new RuntimeException("Failed to create Pipeline Layout"), "PipelineLayout@VulkanImpl");
            }

            this.handle = pLayout.get(0);
            this.pushConstants.setHandle(this.handle);
        }
    }

    @Override
    public int pushConstantSize() {
        return (int) pushConstants.getLayout().size;
    }

    @Override
    public int descriptorCount() {
        return userSets.size();
    }

    public long getHandle() { return this.handle; }

    public List<DescriptorSetInstance> getUserSets() { return this.userSets; }

    public DescriptorSets descriptors() {
        return null;
    }

    public PushConstants pushConstants() {
        return pushConstants;
    }

    @Override
    public void free() {
        // destroy descriptors and stuff
        VK14.vkDestroyPipelineLayout(device.getLogicalDevice().getDevice(), this.handle, null);
        this.alloc.free();
        this.userSets.forEach(Disposable::free);
        pushConstants.free();
    }

    public record LayoutCapabilities() {}

}
