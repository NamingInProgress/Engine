package com.vke.core.vulkan.descriptor;

import com.vke.api.rendering.abstraction.data.Texture;
import com.vke.api.rendering.abstraction.enums.buffer.BufferUsage;
import com.vke.api.rendering.vulkan.descriptors.info.BindingLayout;
import com.vke.api.rendering.vulkan.descriptors.info.DescriptorSetLayout;
import com.vke.core.Context;
import com.vke.core.vulkan.buffers.MappedGpuRingBuffer;
import com.vke.core.vulkan.descriptor.data.ShaderDataManager;
import com.vke.core.vulkan.descriptor.dynamicalloc.DynamicDescriptorAllocator;
import com.vke.core.vulkan.device.VulkanRenderDevice;
import com.vke.core.vulkan.service.VulkanRenderer;
import com.vke.core.vulkan.shr.ReflectedShader;
import com.vke.utils.io.Disposable;
import com.vke.utils.iter.Iter;
import com.vke.utils.tuple.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class EngineDescriptorSetsManager implements Disposable {

    public final HashMap<Integer, DescriptorSetLayout> ENGINE_LAYOUTS = new HashMap<>();
    public Integer[] usedSets;

    private final HashMap<Pair<Integer, Integer>, MappedGpuRingBuffer> BUFFERS = new HashMap<>(); // <Set, Binding>

    private Texture[] bindlessTextures;

    public EngineDescriptorSetsManager(Context context, VulkanRenderer renderer, VulkanRenderDevice device, ReflectedShader truth) {
        for (Map.Entry<ReflectedShader.ResourceType, ArrayList<ReflectedShader.DescriptorResource>> entry : truth.getDescriptors().entrySet()) {
            for (ReflectedShader.DescriptorResource descriptorResource : entry.getValue()) {
                ENGINE_LAYOUTS.computeIfAbsent(descriptorResource.set, (_) -> new DescriptorSetLayout())
                        .bindings.add(BindingLayout.fromDescriptorResource(descriptorResource, entry.getKey(),
                                !truth.getMetadata().staticBuffers().contains(descriptorResource.name)));

                if (entry.getKey().isDescriptorBuffer()) {
                    var bufferUsage = entry.getKey() == ReflectedShader.ResourceType.UBO ? BufferUsage.Bits.UBO.into() : BufferUsage.Bits.SSBO.into();
                    BUFFERS.put(new Pair<>(descriptorResource.set, descriptorResource.binding),
                            new MappedGpuRingBuffer(context.getEngine(), device,
                                    descriptorResource.struct.size * truth.getMetadata().multipleWrites().getOrDefault(descriptorResource.name, 1),
                                    renderer.getFrameCounter().framesInFlight(), bufferUsage));
                }
            }
        }
        usedSets = Iter.of(ENGINE_LAYOUTS.keySet()).toArray();

        initialize();
    }

    private void initialize() {
        bindlessTextures = new Texture[4096]; // device.capabilities
    }

    public int texture(Texture tex) {
        int firstFree = -1;
        for (int i = 0; i < bindlessTextures.length; i++) {
            if (bindlessTextures[i] == tex) return i;
            if (bindlessTextures[i] == null && firstFree == -1) {
                firstFree = i;
            }
        }
        if (firstFree == -1) throw new IllegalStateException("Out of texture slots!");

        bindlessTextures[firstFree] = tex;
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
        ShaderDataManager.getInstance().free();
    }
}
