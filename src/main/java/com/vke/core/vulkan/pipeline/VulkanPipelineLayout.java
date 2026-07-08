package com.vke.core.vulkan.pipeline;

import com.carrotsearch.hppc.ObjectIntHashMap;
import com.vke.api.rendering.FrameCounter;
import com.vke.api.rendering.abstraction.pipeline.PipelineLayout;
import com.vke.api.rendering.vulkan.descriptors.DescriptorType;
import com.vke.api.rendering.vulkan.descriptors.info.DescriptorSetLayout;
import com.vke.api.rendering.vulkan.descriptors2.DescriptorSetGroup;
import com.vke.api.rendering.vulkan.descriptors2.handles.UniformHandle;
import com.vke.api.rendering.vulkan.descriptors2.handles.other.CISHandle;
import com.vke.api.rendering.vulkan.descriptors2.handles.other.ImageHandle;
import com.vke.api.rendering.vulkan.descriptors2.handles.other.SamplerHandle;
import com.vke.api.rendering.vulkan.descriptors2.handles.other.array.CISArrayHandle;
import com.vke.api.rendering.vulkan.descriptors2.handles.other.array.ImageArrayHandle;
import com.vke.api.rendering.vulkan.descriptors2.handles.other.array.SamplerArrayHandle;
import com.vke.api.rendering.vulkan.pushconstants.PushConstants;
import com.vke.core.VKEngine;
import com.vke.core.vulkan.descriptor.DescriptorAllocator;
import com.vke.core.vulkan.descriptor.DescriptorWriter;
import com.vke.core.vulkan.descriptor.EngineDescriptorSetsManager;
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
    private final EngineDescriptorSetsManager engineSets;

    private final PushConstants pushConstants;

    private final DescriptorAllocator alloc;
    private final List<DescriptorSetInstance> userSets = new ArrayList<>();

    private DescriptorSetGroup group;

    public final DescriptorWriter writer;

    public static VulkanPipelineLayout getLayout(VKEngine engine, VulkanRenderDevice device, PushConstants pc, List<DescriptorSetLayout> layouts) {
        // TODO: Fix this making a new pipeline layout (This is technically fine but it is recommended to reuse)
        FrameCounter fc = device.getRenderer().getFrameCounter();
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
        this.writer = new DescriptorWriter(device);
        this.engineSets = device.getRenderer().getEngineSetsManager();

        ObjectIntHashMap<DescriptorType> counts = new ObjectIntHashMap<>();
        layouts.forEach(setLayout -> setLayout.bindings.forEach(bindingLayout ->
                counts.addTo(bindingLayout.type, bindingLayout.descriptorCount)));

        this.alloc = new DescriptorAllocator(engine, device, counts, layouts.size(), fc.framesInFlight(), false);

        int engineSetsEnd = engineSets.ENGINE_PIPELINE_LAYOUT == null ? 0 : engineSets.highestSet + 1;

        if (engineSetsEnd == 0) {
            for (int i = 0; i < layouts.size(); i++) {
                var ds = new DescriptorSetInstance(engine, device, alloc, layouts.get(i), fc, i);
                engineSets.INSTANCES.add(ds);
                userSets.add(ds);
            }
            engineSetsEnd = engineSets.highestSet + 1;
        } else {
            userSets.addAll(engineSets.INSTANCES);
        }

        for (int i = engineSetsEnd; i < layouts.size(); i++) {
            DescriptorSetLayout layout = layouts.get(i);
            var ds = new DescriptorSetInstance(engine, device, alloc, layout, fc, i);
            userSets.add(ds);
        }

        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkPipelineLayoutCreateInfo createInfo = VkPipelineLayoutCreateInfo.calloc(stack)
                    .sType$Default();

            VkPushConstantRange.Buffer pushConstantsBuffer = null;
            if (pc != null && pc.getLayout().size != 0) {
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
            if (this.pushConstants != null)
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

    public PushConstants pushConstants() {
        return pushConstants;
    }

    public DescriptorSetGroup getGroup() {
        if (group == null) group = new DescriptorSetGroup(this, device.getRenderer().getFrameCounter());
        return group;
    }

    public long getSetHandle(int set) {
        return getUserSets().get(set).getSet().getHandle();
    }

    public void writeHandles() {
        getGroup().getDirtyHandles().addAll(engineSets.ENGINE_PIPELINE_LAYOUT.getGroup().getDirtyHandles());

        if (!getGroup().getDirtyHandles().isEmpty()) {
            for (UniformHandle uh : getGroup().getDirtyHandles()) {
                long dsh = getSetHandle(uh.set);
                switch (uh) {
                    case CISHandle handle ->
                            writer.writeCombinedImageSamplers(dsh, handle.binding, handle.cisBinding.textures, handle.cisBinding.samplers);
                    case CISArrayHandle handle ->
                            writer.writeCombinedImageSamplers(dsh, handle.binding, handle.cisBinding.textures, handle.cisBinding.samplers);
                    case ImageHandle handle ->
                            writer.writeImages(dsh, handle.binding, handle.imgBinding.textures, handle.type);
                    case ImageArrayHandle handle ->
                            writer.writeImages(dsh, handle.binding, handle.imgBinding.textures, handle.type);
                    case SamplerHandle handle ->
                            writer.writeSamplers(dsh, handle.binding, handle.samplBinding.samplers);
                    case SamplerArrayHandle handle ->
                            writer.writeSamplers(dsh, handle.binding, handle.samplBinding.samplers);
                    default -> {
                    }
                }
            }

            getGroup().clearDirty();
            engineSets.ENGINE_PIPELINE_LAYOUT.getGroup().clearDirty();
            writer.flush();
        }
    }

    @Override
    public void free() {
        // destroy descriptors and stuff
        this.userSets.forEach(Disposable::free);
        this.alloc.free();
        if (pushConstants != null) {
            pushConstants.free();
        }
        VK14.vkDestroyPipelineLayout(device.getLogicalDevice().getDevice(), this.handle, null);
    }

    public record LayoutCapabilities() {}

}
