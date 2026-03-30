package com.vke.core.vulkan.pipeline;

import com.carrotsearch.hppc.cursors.IntObjectCursor;
import com.vke.api.pipeline.BaseType;
import com.vke.api.rendering.abstraction.enums.buffer.BufferUsage;
import com.vke.api.rendering.abstraction.enums.buffer.PackingType;
import com.vke.api.pipeline.DescriptorData;
import com.vke.api.rendering.vulkan.descriptors.handles.parsing.HandleParser;
import com.vke.api.rendering.vulkan.descriptors.handles.parsing.node.ArrayIndexNode;
import com.vke.api.rendering.vulkan.descriptors.handles.parsing.node.BaseNode;
import com.vke.api.rendering.vulkan.descriptors.handles.parsing.node.Node;
import com.vke.api.rendering.vulkan.descriptors.handles.*;
import com.vke.api.rendering.vulkan.descriptors.handles.array.BufferArrayHandle;
import com.vke.api.rendering.vulkan.descriptors.handles.array.CombinedImageSamplerArrayHandle;
import com.vke.api.rendering.vulkan.descriptors.handles.array.ImageArrayHandle;
import com.vke.api.rendering.vulkan.descriptors.handles.single.BufferHandle;
import com.vke.api.rendering.vulkan.descriptors.handles.single.CombinedImageSamplerHandle;
import com.vke.api.rendering.vulkan.descriptors.handles.single.ImageHandle;
import com.vke.core.VKEngine;
import com.vke.core.vulkan.buffers.MappedBuffer;
import com.vke.core.vulkan.descriptor.DescriptorWriter;
import com.vke.core.vulkan.device.VulkanRenderDevice;
import com.vke.core.vulkan.shader.VulkanShader;
import com.vke.utils.Disposable;
import com.vke.utils.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class VulkanDescriptorData extends DescriptorData {

    private static final HandleParser parser = new HandleParser();

    private final ArrayList<MappedBuffer> buffers = new ArrayList<>();

    private final VKEngine engine;
    private final VulkanRenderDevice device;

    private final DescriptorWriter writer;

    public VulkanDescriptorData(VKEngine engine, VulkanRenderDevice device) {
        this.engine = engine;
        this.device = device;
        this.writer = new DescriptorWriter(device);
    }

    public Set initAndAddDescriptorSet(long setHandle, int setIndex, List<Pair<Binding, VulkanShader.Stages>> bindingData) {
        VulkanSet set = new VulkanSet();
        set.set = setIndex;
        set.handle = setHandle;

        AtomicInteger i = new AtomicInteger(0);
        bindingData.forEach((pair) -> {
            Binding binding = pair.v1;
            VulkanShader.Stages stages = pair.v2;
            if (binding.type.isBuffer()) {
                BufferBinding bufferBinding = (BufferBinding) binding;
                int totalSize = Math.abs(binding.struct.sizeof() * bufferBinding.descriptorCount);
                BufferUsage usage = new BufferUsage(binding.type == Binding.Type.STORAGE_BUFFER ? BufferUsage.Bits.SSBO : BufferUsage.Bits.UBO);
                MappedBuffer buffer = new MappedBuffer(engine, device, totalSize, usage);
                buffers.add(buffer);

                set.bindings.put(i.getAndIncrement(), new BufferBinding(bufferBinding, stages, buffer.getMappedAddress(), buffer.getGpuBuffer().getBuffer(), bufferBinding.descriptorCount, totalSize));
            } else {
                set.bindings.put(i.getAndIncrement(), new ImageBinding(binding, stages, binding.descriptorCount));
            }
        });

        sets.put(set.getSet(), set);
        return set;
    }

    @Override
    protected UniformHandle createHandle(String name) {
        BaseNode master = parser.parse(name);
        BindingNode bindingNode = master.child;
        Node child = bindingNode.child;

        if (child == null) return fastEOL(bindingNode.name);

        Pair<Set, Binding> base = getBase(bindingNode.name);
        VulkanSet set = (VulkanSet) base.v1;
        Binding binding = base.v2;
        ResolveState state = new ResolveState();
        state.setHandle = set.handle;
        state.binding = binding.binding;
        state.type = binding.type;

        if (child instanceof ArrayIndexNode ain) {
            if (binding.descriptorCount > 1) {
                state.descriptorArrayIndex = ain.index;
                child = ain.child;
            } else {
                throw new IllegalStateException("Cannot index into non array descriptor!");
            }
        }

        while (child != null) {
            if (child instanceof ArrayIndexNode ain) {

            }

            child = child.child;
        }

        return null;
    }

    public static class ResolveState {
        long setHandle;
        int binding;
        Binding.Type type;
        int descriptorArrayIndex = -1;

        BaseType baseType;
        long offset = 0;
    }

    public UniformHandle fastEOL(String bindingName) {
        Pair<Set, Binding> base = getBase(bindingName);
        if (base == null) throw new IllegalStateException("Unknown uniform " + bindingName);

        VulkanSet set = (VulkanSet) base.v1;
        Binding binding = base.v2;

        return switch (binding.type) {
            case COMBINED_IMAGE_SAMPLER -> getFromCISType(set, (ImageBinding) binding);
            case STORAGE_IMAGE -> getFromSIType(set, (ImageBinding) binding);
            case UNIFORM_BUFFER, STORAGE_BUFFER -> getFromBufferType(set, (BufferBinding) binding, binding.type);
        };
    }

    public UniformHandle getFromCISType(VulkanSet set, ImageBinding binding) {
        if (binding.descriptorCount == -1) { // -1 specifies it is a single sampler not a sampler[]
            return new CombinedImageSamplerHandle(set.handle, binding.binding, Binding.Type.COMBINED_IMAGE_SAMPLER, PackingType.STD140, -1);
        } else {
            return new CombinedImageSamplerArrayHandle(set.handle, binding.binding, Binding.Type.COMBINED_IMAGE_SAMPLER, PackingType.STD140, binding.descriptorCount);
        }
    }

    public UniformHandle getFromSIType(VulkanSet set, ImageBinding binding) {
        if (binding.descriptorCount == -1) { // -1 specifies it is a single image not a image[]
            return new ImageHandle(set.handle, binding.binding, Binding.Type.STORAGE_IMAGE, PackingType.STD140, -1);
        } else {
            return new ImageArrayHandle(set.handle, binding.binding, Binding.Type.STORAGE_IMAGE, PackingType.STD140, binding.descriptorCount);
        }
    }

    public UniformHandle getFromBufferType(VulkanSet set, BufferBinding binding, Binding.Type type) {
        if (binding.descriptorCount == -1) { // -1 specifies it is a single buffer not a buffer[]
            return new BufferHandle(set.handle, binding.binding, type, PackingType.STD140, -1, binding.struct.sizeof(), binding.buf, binding.gpuBuf);
        } else {
            return new BufferArrayHandle(set.handle, binding.binding, type, PackingType.STD140, binding.descriptorCount, binding.struct.sizeof(), binding.totalSize, binding.buf, binding.gpuBuf);
        }
    }

    private Pair<Set, Binding> getBase(String base) {
        for (IntObjectCursor<Set> setCursor : sets) {
            for (IntObjectCursor<Binding> bindingCursor : setCursor.value.bindings) {
                if (!bindingCursor.value.name.equals(base)) continue;

                return new Pair<>(setCursor.value, bindingCursor.value);
            }
        }

        return null;
    }

    public void flush(UniformHandle handle) {
        //switch (handle.bindingType) {
        //    case STORAGE_BUFFER, UNIFORM_BUFFER -> {
        //        LoggerFactory.get("Vulkan Descriptor Set").warn("Flushing a buffer uniform (setting the vulkan binding and not writing data). Are you sure this is correct?");
        //        writer.writeBuffer(handle.setHandle, handle.binding, handle.size, handle.offset, handle.gpuBuffer, handle.bindingType);
        //    }
        //    case COMBINED_IMAGE_SAMPLER -> writer.writeSamplers(handle.setHandle, handle.binding, (VulkanTexture[]) handle.textures, (VulkanSampler[]) handle.samplers);
        //    case STORAGE_IMAGE -> writer.writeImages(handle.setHandle, handle.binding, (VulkanTexture[]) handle.textures);
        //}

    }

    @Override
    public void free() {
        buffers.forEach(Disposable::free);
    }

    public static class VulkanSet extends DescriptorData.Set {

        public long handle;

    }

}
