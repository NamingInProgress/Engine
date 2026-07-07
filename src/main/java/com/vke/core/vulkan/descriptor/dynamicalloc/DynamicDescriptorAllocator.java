package com.vke.core.vulkan.descriptor.dynamicalloc;

import com.carrotsearch.hppc.ObjectIntHashMap;
import com.vke.api.assets.AssetHandle;
import com.vke.api.rendering.abstraction.pipeline.RenderPipeline;
import com.vke.api.rendering.vulkan.descriptors.DescriptorType;
import com.vke.api.rendering.vulkan.descriptors.handles.UniformHandle;
import com.vke.api.rendering.vulkan.descriptors.handles.array.BufferArrayHandle;
import com.vke.api.rendering.vulkan.descriptors.handles.array.EntryArrayHandle;
import com.vke.api.rendering.vulkan.descriptors.handles.single.BufferHandle;
import com.vke.api.rendering.vulkan.descriptors.info.DescriptorsInfo;
import com.vke.api.rendering.vulkan.descriptors.sets.DescriptorSet;
import com.vke.core.Context;
import com.vke.core.rendering.draw.DrawContext;
import com.vke.core.vulkan.descriptor.CompiledDescriptorSetLayout;
import com.vke.core.vulkan.descriptor.DescriptorPool;
import com.vke.core.vulkan.descriptor.DescriptorWriter;
import com.vke.core.vulkan.device.VulkanRenderDevice;
import com.vke.utils.io.Disposable;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK14;
import org.lwjgl.vulkan.VkDescriptorSetAllocateInfo;

import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.Arrays;

public class DynamicDescriptorAllocator implements Disposable {

    private final ArrayList<DescriptorPool> fullPools = new ArrayList<>();
    private final ArrayList<DescriptorPool> readyPools = new ArrayList<>();
    private final ObjectIntHashMap<DescriptorType> counts;

    private final ArrayList<DescriptorSet> allocatedSets = new ArrayList<>();

    private final Context context;
    private final VulkanRenderDevice device;
    private final DescriptorWriter writer;

    private final boolean updateAfterBind;

    private int setsPerPool;

    public DynamicDescriptorAllocator(Context context, VulkanRenderDevice device, int initialSets, ObjectIntHashMap<DescriptorType> counts, boolean updateAfterBind) {
        this.context = context;
        this.device = device;
        this.counts = counts;
        this.setsPerPool = initialSets;
        this.writer = new DescriptorWriter(device);
        this.updateAfterBind = updateAfterBind;

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
            int result = VK14.vkAllocateDescriptorSets(device.getLogicalDevice().getDevice(), info, pSet);

            if (result == VK14.VK_ERROR_OUT_OF_POOL_MEMORY || result == VK14.VK_ERROR_FRAGMENTED_POOL) {
                fullPools.add(pool);

                pool = getPool();
                info.descriptorPool(pool.getHandle());
                if (VK14.vkAllocateDescriptorSets(device.getLogicalDevice().getDevice(), info, pSet) != VK14.VK_SUCCESS) {
                    context.throwException(new IllegalStateException("Failed to create descriptor set! (You know its bad when the tutorial says this case is so bad you're better off crashing) " + result), "DynamicDescriptorAllocator");
                }
            }
            readyPools.add(pool);

            DescriptorSet ds = new DescriptorSet(pSet.get(0), device, context.getEngine(), layout.getLayout());
            allocatedSets.add(ds);

            return ds;
        }
    }

    public void update(UniformHandle... uniforms) {
        for (UniformHandle uniform : uniforms) {
            if (uniform instanceof BufferArrayHandle || uniform instanceof BufferHandle || uniform instanceof EntryArrayHandle) {
                continue;
            }

            uniform.writeDescriptor(writer, allocatedSets.get(uniform.descriptorSetListIndex).handle);
        }

        writer.flush();
    }

    public void bindDescriptors(DrawContext ctx, AssetHandle<RenderPipeline> pipeline, UniformHandle... handles) {
        //ctx.getCommandBuffer().bindDescriptorSets(pipeline, Arrays.stream(handles).mapToLong(handle -> allocatedSets.get(handle.descriptorSetListIndex).handle).toArray());
    }

    public <T extends UniformHandle> T copy(UniformHandle toCopy) {
        UniformHandle handle = toCopy.copy();
        allocate(handle.compiledLayout);
        handle.descriptorSetListIndex = allocatedSets.size() - 1;
        return (T) handle;
    }

    protected DescriptorPool getPool() {
        DescriptorPool pool = null;

        if (!readyPools.isEmpty()) {
            pool = readyPools.get(0);
            readyPools.remove(pool);
        } else {
            pool = createPool(setsPerPool);

            setsPerPool = (int) (setsPerPool * 1.5);
            if (setsPerPool > 4096) setsPerPool = 4096;
        }

        return pool;
    }

    protected DescriptorPool createPool(int sets) {
        return new DescriptorPool(context.getEngine(), device, counts, sets, 1, this.updateAfterBind);
    }

    public void clear() {
        readyPools.forEach(DescriptorPool::reset);
        fullPools.forEach((pool) -> {
            pool.reset();
            readyPools.add(pool);
        });
        fullPools.clear();
    }

    @Override
    public void free() {
        readyPools.forEach(DescriptorPool::free);
        fullPools.forEach(DescriptorPool::free);
    }

}
