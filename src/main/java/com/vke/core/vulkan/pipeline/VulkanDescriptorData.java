package com.vke.core.vulkan.pipeline;

import com.carrotsearch.hppc.cursors.IntObjectCursor;
import com.vke.api.abstraction.data.Sampler;
import com.vke.api.abstraction.data.Texture;
import com.vke.api.abstraction.descriptors.buffer.BufferUsage;
import com.vke.api.abstraction.descriptors.buffer.PackingType;
import com.vke.api.pipeline.DescriptorData;
import com.vke.api.pipeline.Entry;
import com.vke.api.pipeline.handles.UniformHandle;
import com.vke.core.VKEngine;
import com.vke.core.logger.LoggerFactory;
import com.vke.core.vulkan.buffers.MappedBuffer;
import com.vke.core.vulkan.descriptor.DescriptorWriter;
import com.vke.core.vulkan.device.VulkanRenderDevice;
import com.vke.core.vulkan.sampler.VulkanSampler;
import com.vke.core.vulkan.shader.Shader;
import com.vke.core.vulkan.texture.VulkanTexture;
import com.vke.utils.Disposable;
import com.vke.utils.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class VulkanDescriptorData extends DescriptorData {

    private final ArrayList<MappedBuffer> buffers = new ArrayList<>();

    private final VKEngine engine;
    private final VulkanRenderDevice device;

    private final DescriptorWriter writer;

    public VulkanDescriptorData(VKEngine engine, VulkanRenderDevice device) {
        this.engine = engine;
        this.device = device;
        this.writer = new DescriptorWriter(device);
    }

    public Set initAndAddDescriptorSet(long setHandle, int setIndex, List<Pair<Binding, Shader.Stages>> bindingData) {
        VulkanSet set = new VulkanSet();
        set.set = setIndex;
        set.handle = setHandle;

        AtomicInteger i = new AtomicInteger(0);
        bindingData.forEach((pair) -> {
            MappedBuffer buffer = null;

            if (pair.v1.type.isBuffer()) {
                BufferUsage usage = new BufferUsage(pair.v1.type == Binding.Type.STORAGE_BUFFER ? BufferUsage.Bits.SSBO : BufferUsage.Bits.UBO);
                buffer = new MappedBuffer(engine, device, pair.v1.struct.sizeof(), usage);
                buffers.add(buffer);
            }

            Binding binding = new Binding(pair.v1, pair.v2, buffer == null ? 0 : buffer.getMappedAddress(), buffer == null ? 0 : buffer.getGpuBuffer().getBuffer());
            set.bindings.put(i.getAndIncrement(), binding);
        });

        sets.put(set.getSet(), set);
        return set;
    }

    @Override
    protected UniformHandle createHandle(String name) {
        String[] split = name.split("\\.");
        if (split.length == 1) split = new String[]{ split[0], split[0] };
        Entry e = null;
        Binding binding = null;
        int set = 0, bindingIdx = 0;
        long setHandle = 0;

        outer:
        for (IntObjectCursor<Set> setCursor : sets) {
            for (IntObjectCursor<Binding> bindingCursor : setCursor.value.bindings) {
                if (!bindingCursor.value.name.equals(split[0])) continue;

                e = bindingCursor.value.struct.getEntry(split[1]);
                set = setCursor.key;
                bindingIdx = bindingCursor.key;
                binding = bindingCursor.value;
                setHandle = ((VulkanSet) setCursor.value).handle;
                break outer;
            }
        }

        if (e == null) return null;

        UniformHandle handle = new UniformHandle();
        handle.set = set;
        handle.binding = bindingIdx;
        handle.size = e.size;
        handle.offset = e.offset;
        handle.packing = PackingType.STD140;
        handle.bindingType = binding.type;
        handle.buffer = binding.buf;
        handle.setHandle = setHandle;
        if (binding.textureCount > 0) {
            handle.textures = new Texture[binding.textureCount];
            if (binding.type == Binding.Type.COMBINED_IMAGE_SAMPLER)
                handle.samplers = new Sampler[binding.textureCount];
        }

        handle.flushCallback = this::flush;

        return handle;
    }

    public void flush(UniformHandle handle) {
        switch (handle.bindingType) {
            case STORAGE_BUFFER, UNIFORM_BUFFER -> {
                LoggerFactory.get("Vulkan Descriptor Set").warn("Flushing a buffer uniform (setting the vulkan binding and not writing data). Are you sure this is correct?");
                writer.writeBuffer(handle.setHandle, handle.binding, handle.size, handle.offset, handle.gpuBuffer, handle.bindingType);
            }
            case COMBINED_IMAGE_SAMPLER -> writer.writeSamplers(handle.setHandle, handle.binding, (VulkanTexture[]) handle.textures, (VulkanSampler[]) handle.samplers);
            case STORAGE_IMAGE -> writer.writeImages(handle.setHandle, handle.binding, (VulkanTexture[]) handle.textures);
        }

    }

    @Override
    public void free() {
        buffers.forEach(Disposable::free);
    }

    public static class VulkanSet extends DescriptorData.Set {

        public long handle;

    }

}
