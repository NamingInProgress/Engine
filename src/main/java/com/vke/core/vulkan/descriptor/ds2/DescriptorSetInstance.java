package com.vke.core.vulkan.descriptor.ds2;

import com.vke.api.rendering.FrameCounter;
import com.vke.api.rendering.abstraction.enums.buffer.BufferUsage;
import com.vke.api.rendering.vulkan.descriptors.DescriptorType;
import com.vke.api.rendering.vulkan.descriptors.bindings.*;
import com.vke.api.rendering.vulkan.descriptors.info.BindingLayout;
import com.vke.api.rendering.vulkan.descriptors.info.DescriptorSetLayout;
import com.vke.api.rendering.vulkan.descriptors.sets.DescriptorSet;
import com.vke.core.VKEngine;
import com.vke.core.vulkan.buffers.MappedBuffer;
import com.vke.core.vulkan.buffers.MappedGpuRingBuffer;
import com.vke.core.vulkan.descriptor.CompiledDescriptorSetLayout;
import com.vke.core.vulkan.descriptor.DescriptorAllocator;
import com.vke.core.vulkan.device.VulkanRenderDevice;
import com.vke.core.vulkan.service.VulkanRenderSystem;
import com.vke.utils.Utils;
import com.vke.utils.io.Disposable;

import java.util.HashMap;

public class DescriptorSetInstance implements Disposable {

    public final HashMap<String, DescriptorBinding> bindings = new HashMap<>();
    private final DescriptorSet[] setObjects;
    private final int set;
    private final CompiledDescriptorSetLayout compiledLayout;
    private final FrameCounter fc;
    private final VulkanRenderSystem ctx;

    public DescriptorSetInstance(VulkanRenderSystem ctx, DescriptorAllocator alloc,
                                 DescriptorSetLayout setLayout, FrameCounter fc, int set) {
        this.fc = fc;
        this.set = set;
        this.setObjects = new DescriptorSet[fc.framesInFlight()];
        this.ctx = ctx;

        this.compiledLayout = new CompiledDescriptorSetLayout(ctx, setLayout, null);

        setLayout.bindings.forEach(bindingLayout -> {
            DescriptorBinding binding = createDescriptorBinding(bindingLayout);
            bindings.put(bindingLayout.name, binding);
        });

        for (int i = 0; i < fc.framesInFlight(); i++) {
            setObjects[i] = new DescriptorSet(alloc.allocate(this.compiledLayout));
        }
    }

    public DescriptorSet getSet() {
        return setObjects[fc.currentIndex()];
    }

    public DescriptorSet[] getAllSets() { return this.setObjects; }

    public int set() { return this.set; }

    public CompiledDescriptorSetLayout getCompiledLayout() {
        return this.compiledLayout;
    }

    @Override
    public void free() {
        this.compiledLayout.free();
        bindings.values().forEach(Disposable::free);
    }

    public DescriptorBinding createDescriptorBinding(BindingLayout layout) {
        return switch (layout.type) {
            case UNIFORM_BUFFER, STORAGE_BUFFER, UNIFORM_BUFFER_DYNAMIC, STORAGE_BUFFER_DYNAMIC -> {
                var buffer = generateBuffer(ctx, layout);

                if (buffer == null) throw new RuntimeException("Failed to create buffer while making descriptor bindings!");

                yield new BufferBinding(layout, buffer, layout.typeLayout.size, layout.packingType, layout.multiWrite);
            }
            case COMBINED_IMAGE_SAMPLER -> new CombinedImageSamplerBinding(layout);
            case SAMPLED_IMAGE -> new SampledImageBinding(layout);
            case STORAGE_IMAGE -> new StorageImageBinding(layout);
            case SAMPLER -> new SamplerBinding(layout);
            case ACCELERATION_STRUCTURE -> throw new UnsupportedOperationException("Acceleration structures not implemented!"); // TODO: implement this
        };
    }

    public static MappedBuffer generateBuffer(VulkanRenderSystem ctx, BindingLayout layout) {
        BufferUsage usage = (layout.type == DescriptorType.UNIFORM_BUFFER || layout.type == DescriptorType.UNIFORM_BUFFER_DYNAMIC) ? BufferUsage.Bits.UBO.into() : BufferUsage.Bits.SSBO.into();
        int framesInFlight = ctx.renderer().getFrameCounter().framesInFlight();

        if (layout.staticBuffer) {
            return new MappedBuffer(ctx, layout.typeLayout.size, usage);
        } else {
            return new MappedGpuRingBuffer(ctx, Utils.alignUpFast(layout.typeLayout.size,
                    getAlign(ctx.device(), layout)), framesInFlight * layout.multiWrite, usage);
        }
    }

    public static long getAlign(VulkanRenderDevice device, BindingLayout layout) {
        return layout.type == DescriptorType.UNIFORM_BUFFER || layout.type == DescriptorType.UNIFORM_BUFFER_DYNAMIC
                ? device.capabilities().minUboAlign
                : device.capabilities().minSSBOAlign;
    }

}
