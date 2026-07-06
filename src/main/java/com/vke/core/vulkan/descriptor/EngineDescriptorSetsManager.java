package com.vke.core.vulkan.descriptor;

import com.vke.api.rendering.abstraction.data.Texture;
import com.vke.api.rendering.abstraction.enums.buffer.BufferUsage;
import com.vke.api.rendering.vulkan.descriptors.info.BindingLayout;
import com.vke.api.rendering.vulkan.descriptors.info.DescriptorSetLayout;
import com.vke.api.rendering.vulkan.descriptors2.handles.other.array.CISArrayHandle;
import com.vke.core.Context;
import com.vke.core.vulkan.buffers.MappedGpuRingBuffer;
import com.vke.core.vulkan.descriptor.data.ShaderDataManager;
import com.vke.core.vulkan.descriptor.ds2.DescriptorSetInstance;
import com.vke.core.vulkan.device.VulkanRenderDevice;
import com.vke.core.vulkan.pipeline.VulkanPipelineLayout;
import com.vke.core.vulkan.sampler.Samplers;
import com.vke.core.vulkan.service.VulkanRenderer;
import com.vke.core.vulkan.shr.ReflectedShader;
import com.vke.core.vulkan.utils.VKUtils;
import com.vke.utils.io.Disposable;
import com.vke.utils.iter.Iter;

import java.util.*;
import java.util.stream.Collectors;

public class EngineDescriptorSetsManager implements Disposable {

    public final HashMap<Integer, DescriptorSetLayout> ENGINE_LAYOUTS = new HashMap<>();
    private final HashMap<Long, MappedGpuRingBuffer> BUFFERS = new HashMap<>(); // <Set, Binding>

    public final ArrayList<DescriptorSetInstance> INSTANCES = new ArrayList<>();

    public VulkanPipelineLayout ENGINE_PIPELINE_LAYOUT;

    public Integer[] usedSets;
    public int highestSet; // All sets below this one are used!
    private Texture[] bindlessTextures;
    private CISArrayHandle BINDLESS_HANDLE;

    public EngineDescriptorSetsManager(Context context, VulkanRenderer renderer, VulkanRenderDevice device, ReflectedShader truth) {
        for (Map.Entry<ReflectedShader.ResourceType, ArrayList<ReflectedShader.DescriptorResource>> entry : truth.getDescriptors().entrySet()) {
            for (ReflectedShader.DescriptorResource descriptorResource : entry.getValue()) {
                if (descriptorResource.set > highestSet) highestSet = descriptorResource.set;
                var set = ENGINE_LAYOUTS.computeIfAbsent(descriptorResource.set, (_) -> new DescriptorSetLayout());

                if (descriptorResource.name.equals("textures")) descriptorResource.arrayDim = new int[]{ 4096 }; //device.capabilities and FUCK THIS CODE IS TERRIBLE

                set.bindings.add(BindingLayout.fromDescriptorResource(descriptorResource, entry.getKey(),
                        truth.getMetadata().staticBuffers().contains(descriptorResource.name)));

                if (entry.getKey().isDescriptorBuffer()) {
                    var bufferUsage = entry.getKey() == ReflectedShader.ResourceType.UBO ? BufferUsage.Bits.UBO.into() : BufferUsage.Bits.SSBO.into();
                    // TODO: Change this if it is static to not make a ring buffer
                    BUFFERS.put(VKUtils.encodeDescriptor(descriptorResource.set, descriptorResource.binding),
                            new MappedGpuRingBuffer(context.getEngine(), device,
                                    descriptorResource.struct.size * truth.getMetadata().multipleWrites().getOrDefault(descriptorResource.name, 1),
                                    renderer.getFrameCounter().framesInFlight(), bufferUsage));
                }
            }
        }
        usedSets = Iter.of(ENGINE_LAYOUTS.keySet()).toArray();

        initialize();
    }

    public List<Integer> getDynamicOffsets() {
        return BUFFERS.entrySet().stream()
                .sorted(Comparator.comparingLong(Map.Entry::getKey))
                .mapToInt(entry -> (int) entry.getValue().getOffset())
                .boxed()
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private void initialize() {
        bindlessTextures = new Texture[4096]; // device.capabilities
    }

    public int texture(Texture tex) {
        if (BINDLESS_HANDLE == null) BINDLESS_HANDLE = ENGINE_PIPELINE_LAYOUT.getGroup().resolve("textures");
        int firstFree = -1;
        for (int i = 0; i < bindlessTextures.length; i++) {
            if (bindlessTextures[i] == tex) return i;
            if (bindlessTextures[i] == null && firstFree == -1) {
                firstFree = i;
            }
        }
        if (firstFree == -1) throw new IllegalStateException("Out of texture slots!");

        bindlessTextures[firstFree] = tex;
        BINDLESS_HANDLE.set(tex, Samplers.LINEAR, firstFree);
        return firstFree;
    }

    public void removeTexture(int index) {
        // TODO: implement
    }

    public HashMap<Integer, DescriptorSetLayout> getDefaults() {
        return ENGINE_LAYOUTS;
    }

    @Override
    public void free() {
        ENGINE_PIPELINE_LAYOUT.free();
    }
}
