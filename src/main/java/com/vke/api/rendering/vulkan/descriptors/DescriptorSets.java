package com.vke.api.rendering.vulkan.descriptors;

import com.carrotsearch.hppc.ObjectIntHashMap;
import com.vke.api.logger.Logger;
import com.vke.api.rendering.vulkan.descriptors.bindings.BufferBinding;
import com.vke.api.rendering.vulkan.descriptors.bindings.CombinedImageSamplerBinding;
import com.vke.api.rendering.vulkan.descriptors.bindings.SamplerBinding;
import com.vke.api.rendering.vulkan.descriptors.bindings.image.ImageBinding;
import com.vke.api.rendering.vulkan.descriptors.handles.array.*;
import com.vke.api.rendering.vulkan.descriptors.handles.parsing.HandleParser;
import com.vke.api.rendering.vulkan.descriptors.handles.parsing.LayoutResolver;
import com.vke.api.rendering.vulkan.descriptors.handles.parsing.node.ArrayIndexNode;
import com.vke.api.rendering.vulkan.descriptors.bindings.DescriptorBinding;
import com.vke.api.rendering.vulkan.descriptors.handles.UniformHandle;
import com.vke.api.rendering.vulkan.descriptors.MOVEME.CompiledDescriptorSetLayout;
import com.vke.api.rendering.vulkan.descriptors.MOVEME.DescriptorAllocator;
import com.vke.api.rendering.vulkan.descriptors.handles.parsing.node.EntryNode;
import com.vke.api.rendering.vulkan.descriptors.handles.parsing.node.Node;
import com.vke.api.rendering.vulkan.descriptors.handles.single.*;
import com.vke.api.rendering.vulkan.descriptors.info.BindingLayout;
import com.vke.api.rendering.vulkan.descriptors.info.DescriptorSetLayout;
import com.vke.api.rendering.vulkan.descriptors.info.DescriptorsInfo;
import com.vke.api.rendering.vulkan.descriptors.sets.DescriptorSet;
import com.vke.api.rendering.vulkan.descriptors.types.ArrayType;
import com.vke.api.rendering.vulkan.descriptors.types.StructType;
import com.vke.api.rendering.vulkan.descriptors.types.TypeLayout;
import com.vke.core.VKEngine;
import com.vke.core.logger.LoggerFactory;
import com.vke.core.vulkan.descriptor.DescriptorWriter;
import com.vke.core.vulkan.device.VulkanRenderDevice;
import com.vke.utils.io.Disposable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DescriptorSets implements Disposable {

    public static final Logger logger = LoggerFactory.get("Descriptor Sets");

    private final HashMap<String, UniformHandle> HANDLE_CACHE = new HashMap<>();

    private final List<DescriptorSet> sets = new ArrayList<>();
    private final List<CompiledDescriptorSetLayout> compiledLayouts;

    private final VKEngine engine;
    private final VulkanRenderDevice device;

    private final DescriptorAllocator allocator;

    private final HandleParser parser = new HandleParser();
    private final LayoutResolver layoutResolver = new LayoutResolver();
    private final DescriptorWriter writer;

    public DescriptorSets(VKEngine engine, VulkanRenderDevice device, ArrayList<DescriptorSetLayout> layouts, DescriptorsInfo additionalInfo) {
        this.engine = engine;
        this.device = device;
        this.writer = new DescriptorWriter(device);

        ObjectIntHashMap<DescriptorType> counts = new ObjectIntHashMap<>();

        // Beautiful O(n^2) one liner
        layouts.forEach(setLayout -> setLayout.bindings.forEach(bindingLayout -> counts.addTo(bindingLayout.type, 1)));

        this.allocator = new DescriptorAllocator(engine, device, counts, layouts.size());
        compiledLayouts = layouts.stream().map(dsl -> new CompiledDescriptorSetLayout(engine, device, dsl)).toList();

        for (int i = 0; i < compiledLayouts.size(); i++) {
            sets.add(new DescriptorSet(allocator.allocate(compiledLayouts.get(i)), device, engine, layouts.get(i), additionalInfo));
        }
    }

    public long[] getDescriptorSetHandles() {
        return this.compiledLayouts.stream().mapToLong(CompiledDescriptorSetLayout::getHandle).toArray();
    }

    @SuppressWarnings("unchecked")
    public <T extends UniformHandle> T resolve(String name) {
        if (HANDLE_CACHE.containsKey(name)) return (T) HANDLE_CACHE.get(name);

        UniformHandle handle = createHandle(name);
        if (handle == null) return null;
        HANDLE_CACHE.put(name, handle);
        return (T) handle;
    }

    @SuppressWarnings("unchecked")
    public <T extends UniformHandle> T createHandle(String name) {
        EntryNode root = (EntryNode) parser.parse(name).child;
        DescriptorSet set = null;
        DescriptorBinding binding = null;

        for (DescriptorSet descriptorSet : sets) {
            if (descriptorSet.bindings.containsKey(root.name)) {
                set = descriptorSet;
                binding = descriptorSet.bindings.get(root.name);
                break;
            }
        }

        if (set == null || binding == null) throw new IllegalStateException("Failed to find binding of name " + root.name);

        boolean isDeep = root.child.child instanceof ArrayIndexNode || root.child instanceof EntryNode;
        boolean hasIndex = root.child instanceof ArrayIndexNode;
        int descriptorCount = binding.layout.descriptorCount;

        if (!isDeep) {
            if (binding.layout.type.isBuffer()) {
                return (T) resolveShallowBuffer((BufferBinding) binding, set, root, descriptorCount, hasIndex);
            }
            return (T) resolveShallowNonBuffer(binding, set, root, descriptorCount, hasIndex);
        }

        if (!binding.layout.type.isBuffer()) throw new IllegalStateException("Only Buffer type uniforms support deep access!");

        return (T) resolveDeep((BufferBinding) binding, set, root);
    }

    private UniformHandle resolveDeep(BufferBinding binding, DescriptorSet set, EntryNode ast) {
        boolean isChildArrayNode = ast.child instanceof ArrayIndexNode;
        int descriptorIndex = isChildArrayNode ? ((ArrayIndexNode) ast.child).index : 0;

        Node node = ast.child instanceof ArrayIndexNode ? ast.child.child : ast.child;

        LayoutResolver.LayoutResolution res = layoutResolver.resolveLayoutPath(binding.layout.typeLayout, (EntryNode) node);

        BindingLayout layout = binding.layout;
        long cpuAddress = binding.buffer.getMappedAddress();
        long gpuAddress = binding.buffer.getGpuBuffer().getBuffer();

        if (res.finalType() instanceof ArrayType)
            return new EntryArrayHandle(set.handle, layout.binding, layout.type, layout.packingType, descriptorIndex, binding.singleBufferSize, ((ArrayType) res.finalType()).length, ((ArrayType) res.finalType()).stride, cpuAddress, gpuAddress, res.offset());

        return new EntryHandle(set.handle, layout.binding, layout.type, layout.packingType, descriptorIndex, binding.singleBufferSize, (int) res.finalType().size, cpuAddress, gpuAddress, res.offset()); // I really fucking hope this is correct
    }

    private UniformHandle resolveShallowBuffer(BufferBinding binding, DescriptorSet set, EntryNode node, int descriptorCount, boolean hasIndex) {
        BindingLayout layout = binding.layout;
        if (descriptorCount > 1) {
            if (!hasIndex) {
                return new BufferArrayHandle(set.handle, layout.binding, layout.type, layout.packingType, layout.descriptorCount, binding.singleBufferSize, binding.buffer.getMappedAddress(), binding.buffer.getGpuBuffer().getBuffer());
            }
            return new BufferHandle(set.handle, layout.binding, layout.type, layout.packingType, ((ArrayIndexNode) node.child).index, (int) binding.singleBufferSize, binding.buffer.getMappedAddress(), binding.buffer.getGpuBuffer().getBuffer());
        } else {
            if (hasIndex) throw new IllegalStateException("Requested buffer with index from a non-array buffer!");

            return new BufferHandle(set.handle, layout.binding, layout.type, layout.packingType, 0, (int) binding.singleBufferSize, binding.buffer.getMappedAddress(), binding.buffer.getGpuBuffer().getBuffer());
        }
    }

    private UniformHandle resolveShallowNonBuffer(DescriptorBinding binding, DescriptorSet set, EntryNode node, int descriptorCount, boolean hasIndex) {
        BindingLayout layout = binding.layout;
        if (descriptorCount > 1) {
            if (!hasIndex) {
                return getHandleForNonBufferArrayType(binding, layout, set);
            }
            return getHandleForNonBufferType(binding, layout, set, ((ArrayIndexNode) node.child).index);
        } else {
            if (hasIndex) throw new IllegalStateException("Requested uniform with index from a non-array uniform!");

            return getHandleForNonBufferType(binding, layout, set, 0);
        }
    }

    private UniformHandle getHandleForNonBufferArrayType(DescriptorBinding binding, BindingLayout layout, DescriptorSet set) {
        return switch (layout.type) {
            case COMBINED_IMAGE_SAMPLER -> new CombinedImageSamplerArrayHandle(set.handle, layout.binding, layout.type, null, (CombinedImageSamplerBinding) binding);
            case SAMPLED_IMAGE, STORAGE_IMAGE -> new ImageArrayHandle(set.handle, layout.binding, layout.type, null, (ImageBinding) binding);
            case SAMPLER -> new SamplerArrayHandle(set.handle, layout.binding, layout.type, null, (SamplerBinding) binding);
            case ACCELERATION_STRUCTURE -> throw new UnsupportedOperationException("Acceleration structures unsupported!");
            default -> throw new IllegalStateException("Provided buffer type binding to builder");
        };
    }

    private UniformHandle getHandleForNonBufferType(DescriptorBinding binding, BindingLayout layout, DescriptorSet set, int index) {
        return switch (layout.type) {
            case COMBINED_IMAGE_SAMPLER -> new CombinedImageSamplerHandle(set.handle, layout.binding, layout.type, null, (CombinedImageSamplerBinding) binding, index);
            case SAMPLED_IMAGE, STORAGE_IMAGE -> new ImageHandle(set.handle, layout.binding, layout.type, null, (ImageBinding) binding, index);
            case SAMPLER -> new SamplerHandle(set.handle, layout.binding, layout.type, null, (SamplerBinding) binding, index);
            case ACCELERATION_STRUCTURE -> throw new UnsupportedOperationException("Acceleration structures unsupported!");
            default -> throw new IllegalStateException("Provided buffer type binding to builder");
        };
    }

    public List<CompiledDescriptorSetLayout> getCompiledLayouts() { return this.compiledLayouts; }

    public void update(UniformHandle... uniforms) {
        for (UniformHandle uniform : uniforms) {
            if (uniform instanceof BufferArrayHandle || uniform instanceof BufferHandle || uniform instanceof EntryArrayHandle) {
                logger.warn("Tried updating a buffer based handle!");
                continue;
            }
            uniform.writeDescriptor(writer);
        }

        writer.flush();
    }

    @Override
    public void free() {
        compiledLayouts.forEach(CompiledDescriptorSetLayout::free);
        this.allocator.free();
    }
}
