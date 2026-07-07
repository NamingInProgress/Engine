package com.vke.core.vulkan.descriptor;

import com.vke.api.rendering.vulkan.descriptors.info.BindingLayout;
import com.vke.api.rendering.vulkan.descriptors.info.DescriptorSetLayout;
import com.vke.api.rendering.vulkan.descriptors.sets.DescriptorSet;
import com.vke.core.Context;
import com.vke.core.rendering.texture.VulkanTextureManager;
import com.vke.core.vulkan.buffers.MappedBuffer;
import com.vke.core.vulkan.buffers.MappedGpuRingBuffer;
import com.vke.core.vulkan.descriptor.ds2.DescriptorSetInstance;
import com.vke.core.vulkan.device.VulkanRenderDevice;
import com.vke.core.vulkan.pipeline.VulkanPipelineLayout;
import com.vke.core.vulkan.service.VulkanRenderer;
import com.vke.core.vulkan.shr.ReflectedShader;
import com.vke.core.vulkan.utils.VKUtils;
import com.vke.utils.io.Disposable;
import com.vke.utils.iter.Iter;

import java.util.*;
import java.util.stream.Collectors;

public class EngineDescriptorSetsManager implements Disposable {

    public final HashMap<Integer, DescriptorSetLayout> ENGINE_LAYOUTS = new HashMap<>();
    private final HashMap<Long, MappedBuffer> BUFFERS = new HashMap<>(); // <Set, Binding>

    public final ArrayList<DescriptorSetInstance> INSTANCES = new ArrayList<>();
    public final VulkanTextureManager textureManager;

    public VulkanPipelineLayout ENGINE_PIPELINE_LAYOUT;

    public Integer[] usedSets;
    public int highestSet; // All sets below this one are used!

    public EngineDescriptorSetsManager(Context context, VulkanRenderer renderer, VulkanRenderDevice device, ReflectedShader truth) {
        this.textureManager = new VulkanTextureManager(this, device);

        for (Map.Entry<ReflectedShader.ResourceType, ArrayList<ReflectedShader.DescriptorResource>> entry : truth.getDescriptors().entrySet()) {
            for (ReflectedShader.DescriptorResource descriptorResource : entry.getValue()) {
                if (descriptorResource.set > highestSet) highestSet = descriptorResource.set;
                var set = ENGINE_LAYOUTS.computeIfAbsent(descriptorResource.set, (_) -> new DescriptorSetLayout());

                if (descriptorResource.name.equals("textures")) descriptorResource.arrayDim = new int[]{ VulkanTextureManager.BINDLESS_TEXTURES_COUNT };

                var layout = BindingLayout.fromDescriptorResource(descriptorResource, entry.getKey(),
                        truth.getMetadata().staticBuffers().contains(descriptorResource.name));
                set.bindings.add(layout);

                if (entry.getKey().isDescriptorBuffer()) {
                    BUFFERS.put(VKUtils.encodeDescriptor(descriptorResource.set, descriptorResource.binding),
                            DescriptorSet.generateBuffer(context.getEngine(), device, layout));
                }
            }
        }
        usedSets = Iter.of(ENGINE_LAYOUTS.keySet()).toArray();
    }

    public List<Integer> getDynamicOffsets() {
        return BUFFERS.entrySet().stream()
                .filter(entry -> entry.getValue() instanceof MappedGpuRingBuffer)
                .sorted(Comparator.comparingLong(Map.Entry::getKey))
                .mapToInt(entry -> (int) ((MappedGpuRingBuffer) entry.getValue()).getOffset())
                .boxed()
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public HashMap<Integer, DescriptorSetLayout> getDefaults() {
        return ENGINE_LAYOUTS;
    }

    @Override
    public void free() {
        ENGINE_PIPELINE_LAYOUT.free();
    }
}
