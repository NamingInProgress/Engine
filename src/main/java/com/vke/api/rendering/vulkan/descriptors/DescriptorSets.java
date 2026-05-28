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
import com.vke.core.services2.Services;
import com.vke.core.vulkan.service.VulkanRenderer;
import com.vke.core.vulkan.descriptor.CompiledDescriptorSetLayout;
import com.vke.core.vulkan.descriptor.DescriptorAllocator;
import com.vke.api.rendering.vulkan.descriptors.handles.parsing.node.EntryNode;
import com.vke.api.rendering.vulkan.descriptors.handles.parsing.node.Node;
import com.vke.api.rendering.vulkan.descriptors.handles.single.*;
import com.vke.api.rendering.vulkan.descriptors.info.BindingLayout;
import com.vke.api.rendering.vulkan.descriptors.info.DescriptorSetLayout;
import com.vke.api.rendering.vulkan.descriptors.info.DescriptorsInfo;
import com.vke.api.rendering.vulkan.descriptors.sets.DescriptorSet;
import com.vke.api.rendering.vulkan.descriptors.types.ArrayType;
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

    private final List<DescriptorSet[]> sets = new ArrayList<>();
    private final List<CompiledDescriptorSetLayout> compiledLayouts;

    private final VKEngine engine;
    private final VulkanRenderDevice device;
    private final VulkanRenderer renderer;

    private final DescriptorAllocator allocator;

    private final HandleParser parser = new HandleParser();
    private final LayoutResolver layoutResolver = new LayoutResolver();
    private final DescriptorWriter writer;

    public DescriptorSets(VKEngine engine, VulkanRenderDevice device, ArrayList<DescriptorSetLayout> layouts, DescriptorsInfo additionalInfo) {
        this.engine = engine;
        this.device = device;
        this.renderer = engine.service(Services.VULKAN_RENDERER).assumeImplementation();
        this.writer = new DescriptorWriter(device);

        ObjectIntHashMap<DescriptorType> counts = new ObjectIntHashMap<>();

        // Beautiful O(n^2) one liner
        layouts.forEach(setLayout -> setLayout.bindings.forEach(bindingLayout -> counts.addTo(bindingLayout.type, bindingLayout.descriptorCount)));

        if (layouts.isEmpty()) {
            compiledLayouts = new ArrayList<>();
            allocator = null;
            return;
        }

        this.allocator = new DescriptorAllocator(engine, device, counts, layouts.size(), renderer.getFrameCounter().framesInFlight(), false);
        compiledLayouts = layouts.stream().map(dsl -> new CompiledDescriptorSetLayout(engine, device, dsl, additionalInfo)).toList();

        for (int i = 0; i < compiledLayouts.size(); i++) {
            DescriptorSet[] fifds = new  DescriptorSet[renderer.getFrameCounter().framesInFlight()];
            for (int j = 0; j < renderer.getFrameCounter().framesInFlight(); j++) {
                fifds[j] = new DescriptorSet(allocator.allocate(compiledLayouts.get(i)), device, engine, layouts.get(i), additionalInfo);
            }
            sets.add(fifds);
        }
    }

    public long[] getDescriptorSetHandles() {
        return this.sets.stream().mapToLong((fifds) -> fifds[renderer.getFrameCounter().currentIndex()].getHandle()).toArray();
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
        int descriptorSetIndex = -1;
        DescriptorBinding binding = null;

        for (int i = 0; i < sets.size(); i++) {
            DescriptorSet descriptorSet = sets.get(i)[0];
            if (descriptorSet.bindings.containsKey(root.name)) {
                binding = descriptorSet.bindings.get(root.name);
                descriptorSetIndex = i;
                break;
            }
        }

        if (descriptorSetIndex == -1 || binding == null) throw new IllegalStateException("Failed to resolve binding of name " + root.name);

        boolean isDeep = (root.child != null && root.child.child instanceof ArrayIndexNode) || root.child instanceof EntryNode;
        boolean hasIndex = root.child instanceof ArrayIndexNode;
        int descriptorCount = binding.layout.descriptorCount;

        if (!isDeep) {
            if (binding.layout.type.isBuffer()) {
                return (T) resolveShallowBuffer((BufferBinding) binding, root, descriptorCount, hasIndex, descriptorSetIndex);
            }
            return (T) resolveShallowNonBuffer(binding, root, descriptorCount, hasIndex, descriptorSetIndex);
        }

        if (!binding.layout.type.isBuffer()) throw new IllegalStateException("Only Buffer type uniforms support deep access!");

        return (T) resolveDeep((BufferBinding) binding, root, descriptorSetIndex);
    }

    private UniformHandle resolveDeep(BufferBinding binding, EntryNode ast, int descriptorSetIndex) {
        boolean isChildArrayNode = ast.child instanceof ArrayIndexNode;
        int descriptorIndex = isChildArrayNode ? ((ArrayIndexNode) ast.child).index : 0;
        CompiledDescriptorSetLayout dsl = this.compiledLayouts.get(descriptorSetIndex);

        Node node = ast.child instanceof ArrayIndexNode ? ast.child.child : ast.child;

        LayoutResolver.LayoutResolution res = layoutResolver.resolveLayoutPath(binding.layout.typeLayout, (EntryNode) node);

        BindingLayout layout = binding.layout;
        long cpuAddress = binding.buffer.getMappedAddress();
        long gpuAddress = binding.buffer.getGpuBuffer().getBuffer();

        if (res.finalType() instanceof ArrayType)
            return new EntryArrayHandle(descriptorSetIndex, layout.binding, layout.type, layout.packingType, dsl, descriptorIndex, binding.singleBufferSize, ((ArrayType) res.finalType()).length, ((ArrayType) res.finalType()).stride, cpuAddress, gpuAddress, res.offset());

        return new EntryHandle(descriptorSetIndex, layout.binding, layout.type, layout.packingType, dsl, descriptorIndex, binding.singleBufferSize, (int) res.finalType().size, cpuAddress, gpuAddress, res.offset()); // I really fucking hope this is correct
    }

    private UniformHandle resolveShallowBuffer(BufferBinding binding, EntryNode node, int descriptorCount, boolean hasIndex, int descriptorSetIndex) {
        BindingLayout layout = binding.layout;
        CompiledDescriptorSetLayout dsl = this.compiledLayouts.get(descriptorSetIndex);
        if (descriptorCount > 1) {
            if (!hasIndex) {
                return new BufferArrayHandle(descriptorSetIndex, layout.binding, layout.type, layout.packingType, dsl, layout.descriptorCount, binding.singleBufferSize, binding.buffer.getMappedAddress(), binding.buffer.getGpuBuffer().getBuffer());
            }
            return new BufferHandle(descriptorSetIndex, layout.binding, layout.type, layout.packingType, dsl, ((ArrayIndexNode) node.child).index, (int) binding.singleBufferSize, binding.buffer.getMappedAddress(), binding.buffer.getGpuBuffer().getBuffer());
        } else {
            if (hasIndex) throw new IllegalStateException("Requested buffer with index from a non-array buffer!");

            return new BufferHandle(descriptorSetIndex, layout.binding, layout.type, layout.packingType, dsl, 0, (int) binding.singleBufferSize, binding.buffer.getMappedAddress(), binding.buffer.getGpuBuffer().getBuffer());
        }
    }

    private UniformHandle resolveShallowNonBuffer(DescriptorBinding binding, EntryNode node, int descriptorCount, boolean hasIndex, int descriptorSetIndex) {
        BindingLayout layout = binding.layout;
        if (descriptorCount > 1) {
            if (!hasIndex) {
                return getHandleForNonBufferArrayType(binding, layout, descriptorSetIndex);
            }
            return getHandleForNonBufferType(binding, layout, ((ArrayIndexNode) node.child).index, descriptorSetIndex);
        } else {
            if (hasIndex) throw new IllegalStateException("Requested uniform with index from a non-array uniform!");

            return getHandleForNonBufferType(binding, layout, 0, descriptorSetIndex);
        }
    }

    private UniformHandle getHandleForNonBufferArrayType(DescriptorBinding binding, BindingLayout layout, int descriptorSetIndex) {
        CompiledDescriptorSetLayout dsl = this.compiledLayouts.get(descriptorSetIndex);
        return switch (layout.type) {
            case COMBINED_IMAGE_SAMPLER -> new CombinedImageSamplerArrayHandle(descriptorSetIndex, layout.binding, layout.type, null, dsl, (CombinedImageSamplerBinding) binding);
            case SAMPLED_IMAGE, STORAGE_IMAGE -> new ImageArrayHandle(descriptorSetIndex, layout.binding, layout.type, null, dsl, (ImageBinding) binding);
            case SAMPLER -> new SamplerArrayHandle(descriptorSetIndex, layout.binding, layout.type, null, dsl, (SamplerBinding) binding);
            case ACCELERATION_STRUCTURE -> throw new UnsupportedOperationException("Acceleration structures unsupported!");
            default -> throw new IllegalStateException("Provided buffer type binding to builder");
        };
    }

    private UniformHandle getHandleForNonBufferType(DescriptorBinding binding, BindingLayout layout, int index, int descriptorSetIndex) {
        CompiledDescriptorSetLayout dsl = this.compiledLayouts.get(descriptorSetIndex);
        return switch (layout.type) {
            case COMBINED_IMAGE_SAMPLER -> new CombinedImageSamplerHandle(descriptorSetIndex, layout.binding, layout.type, null, dsl, (CombinedImageSamplerBinding) binding, index);
            case SAMPLED_IMAGE, STORAGE_IMAGE -> new ImageHandle(descriptorSetIndex, layout.binding, layout.type, null, dsl, (ImageBinding) binding, index);
            case SAMPLER -> new SamplerHandle(descriptorSetIndex, layout.binding, layout.type, null, dsl, (SamplerBinding) binding, index);
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
            int frameIdx = renderer.getFrameCounter().currentIndex();
            DescriptorSet[] dsfefif = sets.get(uniform.descriptorSetListIndex);
            uniform.writeDescriptor(writer, dsfefif[frameIdx].handle);
        }

        writer.flush();
    }

    @Override
    public void free() {
        compiledLayouts.forEach(CompiledDescriptorSetLayout::free);
        if (this.allocator != null) this.allocator.free();
    }
}
