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
import com.vke.core.vulkan.buffers.MappedBuffer;
import com.vke.core.vulkan.device.VulkanRenderDevice;
import com.vke.core.vulkan.shader.Shader;
import com.vke.utils.Disposable;
import com.vke.utils.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class VulkanDescriptorData extends DescriptorData {

    private final ArrayList<MappedBuffer> buffers = new ArrayList<>();

    private final VKEngine engine;
    private final VulkanRenderDevice device;

    public VulkanDescriptorData(VKEngine engine, VulkanRenderDevice device) {
        this.engine = engine;
        this.device = device;
    }

    public Set initAndAddDescriptorSet(List<Pair<Binding, Shader.Stages>> bindingData) {
        Set set = new Set();

        AtomicInteger i = new AtomicInteger(0);
        bindingData.forEach((pair) -> {
            MappedBuffer buffer = null;

            if (pair.v1.type == Binding.Type.STORAGE_BUFFER || pair.v1.type == Binding.Type.UNIFORM_BUFFER) {
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
        Entry e = null;
        Binding binding = null;
        int set = 0, bindingIdx = 0;

        outer:
        for (IntObjectCursor<Set> setCursor : sets) {
            for (IntObjectCursor<Binding> bindingCursor : setCursor.value.bindings) {
                if (!bindingCursor.value.name.equals(split[0])) continue;

                e = bindingCursor.value.struct.getEntry(split[1]);
                set = setCursor.key;
                bindingIdx = bindingCursor.key;
                binding = bindingCursor.value;
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
        handle.textures = new Texture[binding.textureCount];
        if (binding.type == Binding.Type.COMBINED_IMAGE_SAMPLER) handle.samplers = new Sampler[binding.textureCount];

        handle.flushCallback = this::flush;

        return handle;
    }

    public void flush(UniformHandle handle) {

    }

    @Override
    public void free() {
        buffers.forEach(Disposable::free);
    }
}
