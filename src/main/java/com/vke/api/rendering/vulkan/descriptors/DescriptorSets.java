package com.vke.api.rendering.vulkan.descriptors;

import com.carrotsearch.hppc.ObjectIntHashMap;
import com.vke.api.pipeline.handles.parsing.HandleParser;
import com.vke.api.pipeline.handles.parsing.node.BindingNode;
import com.vke.api.rendering.vulkan.descriptors.bindings.DescriptorBinding;
import com.vke.api.rendering.vulkan.descriptors.handles.BufferHandle;
import com.vke.api.rendering.vulkan.descriptors.handles.UniformHandle;
import com.vke.api.rendering.vulkan.descriptors.MOVEME.CompiledDescriptorSetLayout;
import com.vke.api.rendering.vulkan.descriptors.MOVEME.DescriptorAllocator;
import com.vke.api.rendering.vulkan.descriptors.info.DescriptorSetLayout;
import com.vke.api.rendering.vulkan.descriptors.info.DescriptorsInfo;
import com.vke.api.rendering.vulkan.descriptors.sets.DescriptorSet;
import com.vke.core.VKEngine;
import com.vke.core.vulkan.device.VulkanRenderDevice;
import com.vke.utils.Disposable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DescriptorSets implements Disposable {

    private final HashMap<String, UniformHandle> HANDLE_CACHE = new HashMap<>();

    private final List<DescriptorSet> sets = new ArrayList<>();
    private final List<CompiledDescriptorSetLayout> compiledLayouts;

    private final VKEngine engine;
    private final VulkanRenderDevice device;

    private final DescriptorAllocator allocator;

    private final HandleParser parser = new HandleParser();

    public DescriptorSets(VKEngine engine, VulkanRenderDevice device, ArrayList<DescriptorSetLayout> layouts, DescriptorsInfo additionalInfo) {
        this.engine = engine;
        this.device = device;

        ObjectIntHashMap<DescriptorType> counts = new ObjectIntHashMap<>();

        // Beautiful O(n^2) one liner
        layouts.forEach(setLayout -> setLayout.bindings.forEach(bindingLayout -> counts.addTo(bindingLayout.type, 1)));

        this.allocator = new DescriptorAllocator(engine, device, counts, layouts.size());
        compiledLayouts = layouts.stream().map(dsl -> new CompiledDescriptorSetLayout(engine, device, dsl)).toList();

        for (int i = 0; i < compiledLayouts.size(); i++) {
            sets.add(new DescriptorSet(allocator.allocate(compiledLayouts.get(i)), device, engine, layouts.get(i), additionalInfo));
        }
    }

    @SuppressWarnings("unchecked")
    public <T extends UniformHandle> T resolve(String name) {
        if (HANDLE_CACHE.containsKey(name)) return (T) HANDLE_CACHE.get(name);

        UniformHandle handle = createHandle(name);
        if (handle == null) return null;
        HANDLE_CACHE.put(name, handle);
        return (T) handle;
    }

    public <T extends UniformHandle> T createHandle(String name) {
        BindingNode node = parser.parse(name).child;
        DescriptorSet set = null;
        DescriptorBinding binding = null;

        for (DescriptorSet descriptorSet : sets) {
            if (descriptorSet.bindings.containsKey(node.name)) {
                set = descriptorSet;
                binding = descriptorSet.bindings.get(node.name);
                break;
            }
        }

        if (set == null || binding == null) throw new IllegalStateException("Failed to find binding of name " + node.name);

        return switch (binding.layout.type) {
            case UNIFORM_BUFFER, STORAGE_BUFFER, UNIFORM_BUFFER_DYNAMIC, STORAGE_BUFFER_DYNAMIC -> null;
            case COMBINED_IMAGE_SAMPLER -> null;
            case SAMPLED_IMAGE -> null;
            case STORAGE_IMAGE -> null;
            case SAMPLER -> null;
            case ACCELERATION_STRUCTURE -> null;
        };
    }

    @Override
    public void free() {
        compiledLayouts.forEach(CompiledDescriptorSetLayout::free);
        this.allocator.free();
    }
}
