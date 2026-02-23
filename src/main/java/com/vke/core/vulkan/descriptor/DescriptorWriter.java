package com.vke.core.vulkan.descriptor;

import com.vke.core.memory.AutoHeapAllocator;
import com.vke.core.vulkan.device.VulkanRenderDevice;
import com.vke.utils.Disposable;
import org.lwjgl.vulkan.VkWriteDescriptorSet;

import java.util.ArrayList;
import java.util.List;

public class DescriptorWriter implements Disposable {

    private final VulkanRenderDevice device;
    private final AutoHeapAllocator alloc;

    private final List<VkWriteDescriptorSet> writeData = new ArrayList<>();

    public DescriptorWriter(VulkanRenderDevice device) {
        this.device = device;
        this.alloc = new AutoHeapAllocator();
    }

    public void writeBuffer() {}

    @Override
    public void free() {
        alloc.close();
    }

}
