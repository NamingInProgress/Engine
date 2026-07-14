package com.vke.core.rendering.vulkan.descriptor;

import com.carrotsearch.hppc.ObjectIntHashMap;
import com.vke.api.rendering.vulkan.descriptors.DescriptorType;
import com.vke.api.rendering.vulkan.descriptors.sets.DescriptorSet;
import com.vke.core.rendering.vulkan.device.VulkanRenderDevice;
import com.vke.core.rendering.vulkan.service.VulkanRenderSystem;
import com.vke.utils.io.Disposable;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK14;
import org.lwjgl.vulkan.VkDescriptorSetAllocateInfo;

import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.LinkedList;

public class DynamicDescriptorAllocator implements Disposable {

    private final ArrayList<DescriptorPool> fullPools = new ArrayList<>();
    private final LinkedList<DescriptorPool> readyPools = new LinkedList<>();
    private final ObjectIntHashMap<DescriptorType> counts;

    private final VulkanRenderSystem sys;

    private final boolean updateAfterBind;
    private final int framesInFlight;

    private int setsPerPool;

    public DynamicDescriptorAllocator(VulkanRenderSystem sys, int initialSets, ObjectIntHashMap<DescriptorType> counts,
                                      int framesInFlight, boolean updateAfterBind) {
        this.sys = sys;
        this.counts = counts;
        this.setsPerPool = initialSets;
        this.updateAfterBind = updateAfterBind;
        this.framesInFlight = framesInFlight;

        readyPools.add(createPool(initialSets));
    }

    public DescriptorSet allocate(CompiledDescriptorSetLayout layout) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            DescriptorPool pool = getPool();

            VkDescriptorSetAllocateInfo info = VkDescriptorSetAllocateInfo.calloc(stack)
                    .sType$Default()
                    .descriptorPool(pool.getHandle())
                    .pSetLayouts(stack.longs(layout.getHandle()));

            LongBuffer pSet = stack.mallocLong(1);
            int result = VK14.vkAllocateDescriptorSets(sys.device().vkLogicalDevice(), info, pSet);

            if (result == VK14.VK_ERROR_OUT_OF_POOL_MEMORY || result == VK14.VK_ERROR_FRAGMENTED_POOL) {
                fullPools.add(pool);

                pool = getPool();
                info.descriptorPool(pool.getHandle());
                if (VK14.vkAllocateDescriptorSets(sys.device().vkLogicalDevice(), info, pSet) != VK14.VK_SUCCESS) {
                    sys.throwException(new IllegalStateException("Failed to create descriptor set! (You know its bad when the tutorial says this case is so bad you're better off crashing) " + result), "DynamicDescriptorAllocator");
                }
            }
            readyPools.add(pool);

            return new DescriptorSet(pSet.get(0));
        }
    }

    protected DescriptorPool getPool() {
        DescriptorPool pool;

        if (!readyPools.isEmpty()) {
            pool = readyPools.pop();
        } else {
            pool = createPool(setsPerPool);

            setsPerPool = (int) (setsPerPool * 1.5);
            if (setsPerPool > 4096) setsPerPool = 4096;
        }

        return pool;
    }

    protected DescriptorPool createPool(int sets) {
        return new DescriptorPool(sys, counts, sets, framesInFlight, this.updateAfterBind);
    }

    @Override
    public void free() {
        readyPools.forEach(DescriptorPool::free);
        fullPools.forEach(DescriptorPool::free);
    }

}
