package com.vke.api.rendering.vulkan.descriptors2;

import com.vke.api.rendering.vulkan.descriptors.bindings.BufferBinding;
import com.vke.api.rendering.vulkan.descriptors.bindings.CombinedImageSamplerBinding;
import com.vke.api.rendering.vulkan.descriptors.bindings.DescriptorBinding;
import com.vke.api.rendering.vulkan.descriptors.bindings.SamplerBinding;
import com.vke.api.rendering.vulkan.descriptors.bindings.image.ImageBinding;
import com.vke.api.rendering.vulkan.descriptors.parsing.HandleParser;
import com.vke.api.rendering.vulkan.descriptors.parsing.LayoutResolver;
import com.vke.api.rendering.vulkan.descriptors.parsing.node.ArrayIndexNode;
import com.vke.api.rendering.vulkan.descriptors.parsing.node.EntryNode;
import com.vke.api.rendering.vulkan.descriptors.parsing.node.Node;
import com.vke.api.rendering.vulkan.descriptors.info.BindingLayout;
import com.vke.api.rendering.vulkan.descriptors.sets.DescriptorSet;
import com.vke.api.rendering.vulkan.descriptors.types.ArrayType;
import com.vke.api.rendering.vulkan.descriptors2.handles.*;
import com.vke.api.rendering.vulkan.descriptors2.handles.buf.*;
import com.vke.api.rendering.vulkan.descriptors2.handles.other.CISHandle;
import com.vke.api.rendering.vulkan.descriptors2.handles.other.ImageHandle;
import com.vke.api.rendering.vulkan.descriptors2.handles.other.SamplerHandle;
import com.vke.api.rendering.vulkan.descriptors2.handles.other.array.CISArrayHandle;
import com.vke.api.rendering.vulkan.descriptors2.handles.other.array.ImageArrayHandle;
import com.vke.api.rendering.vulkan.descriptors2.handles.other.array.SamplerArrayHandle;
import com.vke.core.rendering.vulkan.descriptor.DescriptorWriter;
import com.vke.core.rendering.vulkan.descriptor.ds2.DescriptorSetInstance;
import com.vke.core.rendering.vulkan.pipeline.VulkanPipelineLayout;
import com.vke.core.rendering.vulkan.service.VulkanRenderSystem;

import java.util.*;

public class DescriptorSetGroup {

    private final HashMap<String, UniformHandle> handleCache = new HashMap<>();
    private final HashSet<UniformHandle> dirtyHandles = new HashSet<>();

    private final HandleParser parser = new HandleParser();
    private final LayoutResolver layoutResolver = new LayoutResolver();

    private final VulkanRenderSystem ctx;
    private final VulkanPipelineLayout parent;

    private final DescriptorWriter writer;

    public DescriptorSetGroup(VulkanRenderSystem ctx, VulkanPipelineLayout parent) {
        this.ctx = ctx;
        this.parent = parent;
        this.writer = parent.writer;
    }

    public void scheduleUpdate(UniformHandle handle) {
        ctx.renderer().scheduleDescriptorUpdate(parent, handle);
    }

    public void setDirty(UniformHandle handle) {
        dirtyHandles.add(handle);
    }

    public void clearDirty() {
        this.dirtyHandles.clear();
    }

    public HashSet<UniformHandle> getDirtyHandles() { return this.dirtyHandles; }

    public VulkanRenderSystem getRenderSystem() {
        return ctx;
    }

    public DescriptorSetInstance getSet(int set) { return this.parent.getSets().get(set); }

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
            DescriptorSetInstance descriptorSet = parent.getSets().get(i);
            if (descriptorSet.bindings.containsKey(root.name)) {
                binding = descriptorSet.bindings.get(root.name);
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
                var buf = resolveShallowBuffer((BufferBinding) binding, descriptorCount, hasIndex, set);
                buf.setDirty();
                return (T) buf;
            }
            return (T) resolveShallowNonBuffer(binding, root, descriptorCount, hasIndex, set);
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
            if (binding.multiWrite != 1) {
                return new MultiWriteFieldArrayHandle(this, set.set(), layout.binding, layout.type, (MultiWriteBufferHandle) parent,
                        res.offset(), (int) ((ArrayType) res.finalType()).stride, ((ArrayType) res.finalType()).elementCount);
            }

            return new FieldArrayHandle(this, set.set(), layout.binding, layout.type, parent, res.offset(),
                    (int) ((ArrayType) res.finalType()).stride, ((ArrayType) res.finalType()).elementCount);
        }

        if (binding.multiWrite != 1) {
            return new MultiWriteFieldHandle(this, set.set(), layout.binding, layout.type, (MultiWriteBufferHandle) parent, res.offset(), (int) res.finalType().size);
        }

        return new FieldHandle(this, set.set(), layout.binding, layout.type, parent, res.offset(), (int) res.finalType().size);
    }

    private UniformHandle resolveShallowBuffer(BufferBinding binding, int descriptorCount, boolean hasIndex, DescriptorSetInstance set) {
        BindingLayout layout = binding.layout;

        if (descriptorCount > 1) {
            throw new IllegalStateException("Buffer Arrays not supported!");
//            if (!hasIndex) {
//                return new BufferArrayHandle(descriptorSetIndex, layout.binding, layout.type, layout.packingType, dsl, layout.descriptorCount, binding.singleBufferSize, binding.buffer.getMappedAddress(), binding.buffer.getGpuBuffer().getBuffer());
//            }
//            return new BufferHandle(descriptorSetIndex, layout.binding, layout.type, layout.packingType, dsl, ((ArrayIndexNode) node.child).index, (int) binding.singleBufferSize, binding.buffer.getMappedAddress(), binding.buffer.getGpuBuffer().getBuffer());
        } else {
            if (hasIndex) throw new IllegalStateException("Requested buffer with index from a non-array buffer!");

            for (DescriptorSet ds : set.getAllSets()) {
                writer.writeBuffer(ds.handle(), layout.binding, layout.typeLayout.size, 0, binding.buffer.getGpuBuffer().getBuffer(), layout.type);
            }

            var buf = binding.buffer;
            if (binding.multiWrite == 1) {
                return new BufferHandle(this, set.set(), layout.binding, layout.type, binding, buf.getSize(),
                        buf.getMappedAddress(), buf.getGpuBuffer().getBuffer());
            } else {
                return new MultiWriteBufferHandle(this, set.set(), layout.binding, layout.type, binding, binding.multiWrite,
                        buf.getSize(), binding.singleBufferSize, buf.getMappedAddress(), buf.getGpuBuffer().getBuffer());
            }
        }
    }

    private UniformHandle resolveShallowNonBuffer(DescriptorBinding binding, EntryNode node, int descriptorCount, boolean hasIndex, DescriptorSetInstance instance) {
        BindingLayout layout = binding.layout;
        if (descriptorCount > 1) {
            if (!hasIndex) {
                return getHandleForNonBufferArrayType(binding, layout, instance);
            }
            return getHandleForNonBufferType(binding, layout, ((ArrayIndexNode) node.child).index, instance);
        } else {
            if (hasIndex) throw new IllegalStateException("Requested uniform with index from a non-array uniform!");

            return getHandleForNonBufferType(binding, layout, 0, instance);
        }
    }

    private UniformHandle getHandleForNonBufferArrayType(DescriptorBinding binding, BindingLayout layout, DescriptorSetInstance instance) {
        return switch (layout.type) {
            case COMBINED_IMAGE_SAMPLER -> new CISArrayHandle(this, instance.set(), layout.binding, layout.type, (CombinedImageSamplerBinding) binding);
            case SAMPLED_IMAGE, STORAGE_IMAGE -> new ImageArrayHandle(this, instance.set(), layout.binding, layout.type, (ImageBinding) binding);
            case SAMPLER -> new SamplerArrayHandle(this, instance.set(), layout.binding, layout.type, (SamplerBinding) binding);
            case ACCELERATION_STRUCTURE -> throw new UnsupportedOperationException("Acceleration structures unsupported!");
            default -> throw new IllegalStateException("Provided buffer type binding to builder");
        };
    }

    private UniformHandle getHandleForNonBufferType(DescriptorBinding binding, BindingLayout layout, int index, DescriptorSetInstance instance) {
        return switch (layout.type) {
            case COMBINED_IMAGE_SAMPLER -> new CISHandle(this, instance.set(), layout.binding, layout.type, (CombinedImageSamplerBinding) binding, index);
            case SAMPLED_IMAGE, STORAGE_IMAGE -> new ImageHandle(this, instance.set(), layout.binding, layout.type, (ImageBinding) binding, index);
            case SAMPLER -> new SamplerHandle(this, instance.set(), layout.binding, layout.type, (SamplerBinding) binding, index);
            case ACCELERATION_STRUCTURE -> throw new UnsupportedOperationException("Acceleration structures unsupported!");
            default -> throw new IllegalStateException("Provided buffer type binding to builder");
        };
    }

    public Map<String, UniformHandle> getHandleCache() { return this.handleCache; }

}
