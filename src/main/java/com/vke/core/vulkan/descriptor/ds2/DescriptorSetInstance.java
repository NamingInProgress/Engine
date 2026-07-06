package com.vke.core.vulkan.descriptor.ds2;

import com.vke.api.rendering.FrameCounter;
import com.vke.api.rendering.vulkan.descriptors.info.BindingLayout;
import com.vke.api.rendering.vulkan.descriptors.info.DescriptorSetLayout;
import com.vke.api.rendering.vulkan.descriptors.sets.DescriptorSet;
import com.vke.core.VKEngine;
import com.vke.core.vulkan.descriptor.CompiledDescriptorSetLayout;
import com.vke.core.vulkan.descriptor.DescriptorAllocator;
import com.vke.core.vulkan.device.VulkanRenderDevice;
import com.vke.utils.io.Disposable;

public class DescriptorSetInstance implements Disposable {

    private final DescriptorSet[] setObjects;
    private final int set;
    private final CompiledDescriptorSetLayout compiledLayout;
    private final FrameCounter fc;

    public DescriptorSetInstance(VKEngine engine, VulkanRenderDevice device, DescriptorAllocator alloc,
                                 DescriptorSetLayout setLayout, FrameCounter fc, int set) {
        this.fc = fc;
        this.set = set;
        this.setObjects = new DescriptorSet[fc.framesInFlight()];

        this.compiledLayout = new CompiledDescriptorSetLayout(engine, device, setLayout, null);

        for (int i = 0; i < fc.framesInFlight(); i++) {
            setObjects[i] = new DescriptorSet(alloc.allocate(this.compiledLayout), device, engine, setLayout, null);
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
    }
}
