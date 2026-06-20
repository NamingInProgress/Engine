package com.vke.api.rendering.vulkan.descriptors2;

import com.vke.api.rendering.vulkan.descriptors.bindings.BufferBinding;
import com.vke.api.rendering.vulkan.descriptors.bindings.CombinedImageSamplerBinding;
import com.vke.api.rendering.vulkan.descriptors.bindings.DescriptorBinding;
import com.vke.api.rendering.vulkan.descriptors.bindings.SamplerBinding;
import com.vke.api.rendering.vulkan.descriptors.bindings.image.ImageBinding;
import com.vke.api.rendering.vulkan.descriptors.handles.parsing.HandleParser;
import com.vke.api.rendering.vulkan.descriptors.handles.parsing.LayoutResolver;
import com.vke.api.rendering.vulkan.descriptors.handles.parsing.node.ArrayIndexNode;
import com.vke.api.rendering.vulkan.descriptors.handles.parsing.node.EntryNode;
import com.vke.api.rendering.vulkan.descriptors.handles.parsing.node.Node;
import com.vke.api.rendering.vulkan.descriptors.handles.single.SamplerHandle;
import com.vke.api.rendering.vulkan.descriptors.info.BindingLayout;
import com.vke.api.rendering.vulkan.descriptors.types.ArrayType;
import com.vke.api.rendering.vulkan.descriptors2.handles.*;
import com.vke.core.vulkan.descriptor.CompiledDescriptorSetLayout;
import com.vke.core.vulkan.descriptor.ds2.DescriptorSetInstance;
import com.vke.core.vulkan.pipeline.VulkanPipelineLayout;

import java.util.HashMap;

public class DescriptorSetGroup {

    private final HashMap<String, UniformHandle> handleCache = new HashMap<>();

    private final HandleParser parser = new HandleParser();
    private final LayoutResolver layoutResolver = new LayoutResolver();

    private final VulkanPipelineLayout parent;

    public DescriptorSetGroup(VulkanPipelineLayout parent) {
        this.parent = parent;
    }

    @SuppressWarnings("unchecked")
    public <T extends UniformHandle> T resolve(String path) {
        if (handleCache.containsKey(path)) return (T) handleCache.get(path);

        UniformHandle handle = createHandle(path);
        handleCache.put(path, handle);
        return (T) handle;
    }

    @SuppressWarnings("unchecked")
    public <T extends UniformHandle> T createHandle(String name) {
        EntryNode root = (EntryNode) parser.parse(name).child;
        DescriptorSetInstance set = null;
        DescriptorBinding binding = null;

        for (int i = 0; i < parent.descriptorCount(); i++) {
            DescriptorSetInstance descriptorSet = parent.getUserSets().get(i);
            if (descriptorSet.getSet().bindings.containsKey(root.name)) {
                binding = descriptorSet.getSet().bindings.get(root.name);
                set = descriptorSet;
                break;
            }
        }

        if (set == null || binding == null) throw new IllegalStateException("Failed to resolve binding of name " + root.name);

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

        BufferHandle parent = resolve(name.replaceFirst("[.\\[].*", ""));
        return (T) resolveDeep(parent, (BufferBinding) binding, root, set);
    }

    private UniformHandle resolveDeep(BufferHandle parent, BufferBinding binding, EntryNode ast, DescriptorSetInstance set) {
        Node node = ast.child instanceof ArrayIndexNode ? ast.child.child : ast.child;

        LayoutResolver.LayoutResolution res = layoutResolver.resolveLayoutPath(binding.layout.typeLayout, (EntryNode) node);

        BindingLayout layout = binding.layout;

        if (res.finalType() instanceof ArrayType) {
            if (binding.multiWrite) {
                return new MultiWriteFieldArrayHandle(set, set.set(), layout.binding, layout.type, (MultiWriteBufferHandle) parent,
                        res.offset(), (int) ((ArrayType) res.finalType()).stride, ((ArrayType) res.finalType()).elementCount);
            }

            return new FieldArrayHandle(set, set.set(), layout.binding, layout.type, parent, res.offset(),
                    (int) ((ArrayType) res.finalType()).stride, ((ArrayType) res.finalType()).elementCount);
        }

        if (binding.multiWrite) {
            return new MultiWriteFieldHandle(set, set.set(), layout.binding, layout.type, (MultiWriteBufferHandle) parent, res.offset(), (int) res.finalType().size);
        }

        return new FieldHandle(set, set.set(), layout.binding, layout.type, parent, res.offset(), (int) res.finalType().size);
    }

    private com.vke.api.rendering.vulkan.descriptors.handles.UniformHandle resolveShallowBuffer(BufferBinding binding, EntryNode node, int descriptorCount, boolean hasIndex, int descriptorSetIndex) {
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

    private com.vke.api.rendering.vulkan.descriptors.handles.UniformHandle resolveShallowNonBuffer(DescriptorBinding binding, EntryNode node, int descriptorCount, boolean hasIndex, int descriptorSetIndex) {
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

    private com.vke.api.rendering.vulkan.descriptors.handles.UniformHandle getHandleForNonBufferArrayType(DescriptorBinding binding, BindingLayout layout, int descriptorSetIndex) {
        CompiledDescriptorSetLayout dsl = this.compiledLayouts.get(descriptorSetIndex);
        return switch (layout.type) {
            case COMBINED_IMAGE_SAMPLER -> new CombinedImageSamplerArrayHandle(descriptorSetIndex, layout.binding, layout.type, null, dsl, (CombinedImageSamplerBinding) binding);
            case SAMPLED_IMAGE, STORAGE_IMAGE -> new ImageArrayHandle(descriptorSetIndex, layout.binding, layout.type, null, dsl, (ImageBinding) binding);
            case SAMPLER -> new SamplerArrayHandle(descriptorSetIndex, layout.binding, layout.type, null, dsl, (SamplerBinding) binding);
            case ACCELERATION_STRUCTURE -> throw new UnsupportedOperationException("Acceleration structures unsupported!");
            default -> throw new IllegalStateException("Provided buffer type binding to builder");
        };
    }

    private com.vke.api.rendering.vulkan.descriptors.handles.UniformHandle getHandleForNonBufferType(DescriptorBinding binding, BindingLayout layout, int index, int descriptorSetIndex) {
        CompiledDescriptorSetLayout dsl = this.compiledLayouts.get(descriptorSetIndex);
        return switch (layout.type) {
            case COMBINED_IMAGE_SAMPLER -> new CombinedImageSamplerHandle(descriptorSetIndex, layout.binding, layout.type, null, dsl, (CombinedImageSamplerBinding) binding, index);
            case SAMPLED_IMAGE, STORAGE_IMAGE -> new ImageHandle(descriptorSetIndex, layout.binding, layout.type, null, dsl, (ImageBinding) binding, index);
            case SAMPLER -> new SamplerHandle(descriptorSetIndex, layout.binding, layout.type, null, dsl, (SamplerBinding) binding, index);
            case ACCELERATION_STRUCTURE -> throw new UnsupportedOperationException("Acceleration structures unsupported!");
            default -> throw new IllegalStateException("Provided buffer type binding to builder");
        };
    }

}
