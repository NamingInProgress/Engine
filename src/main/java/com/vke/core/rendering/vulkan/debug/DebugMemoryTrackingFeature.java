package com.vke.core.rendering.vulkan.debug;

import com.vke.api.event.SubscribeEvent;
import com.vke.api.rendering.abstraction.debug.DebugFeature;
import com.vke.core.memory.AutoHeapAllocator;
import com.vke.core.rendering.vulkan.VKEvents;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.vulkan.VkAllocationCallbacks;
import org.lwjgl.vulkan.VkAllocationFunction;
import org.lwjgl.vulkan.VkFreeFunction;
import org.lwjgl.vulkan.VkReallocationFunction;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DebugMemoryTrackingFeature extends DebugFeature {

    public final AutoHeapAllocator alloc;

    private final Map<Long, Allocation> allocations = new ConcurrentHashMap<>();

    public DebugMemoryTrackingFeature() {
        super(DebugFeatures.MEMORY_TRACKER_FEATURE_NAME);
        this.alloc = new AutoHeapAllocator();

        logger.warn("Debug Memory Tracking is turned on. This feature can severely slow your program down, so make sure this is on purpose.");
    }

    @SubscribeEvent
    public void onPreInstanceCreated(VKEvents.PreInstanceCreated event) {
        var pAlloc = alloc.allocStruct(VkAllocationCallbacks.SIZEOF, VkAllocationCallbacks::new);
        pAlloc.pfnAllocation(new VkAllocationFunction() {
            @Override
            public long invoke(long pUserData, long size, long alignment, int allocationScope) {
                long address = MemoryUtil.nmemAlignedAlloc(alignment, size);
                allocations.put(address, new Allocation(address, new Exception().getStackTrace()));
                return address;
            }
        });

        pAlloc.pfnFree(new VkFreeFunction() {
            @Override
            public void invoke(long pUserData, long pMemory) {
                if (pMemory == MemoryUtil.NULL) return;

                if (allocations.containsKey(pMemory)) {
                    allocations.remove(pMemory);
                } else {
                    logger.warn("Potential double free, or freeing non allocated address. Stacktrace:");
                    new Exception().printStackTrace();
                }
                MemoryUtil.nmemFree(pMemory);
            }
        });

        pAlloc.pfnReallocation(new VkReallocationFunction() {
            @Override
            public long invoke(long pUserData, long pOriginal, long size, long alignment, int allocationScope) {
                var obj = allocations.remove(pOriginal);

                if (obj == null) {
                    logger.warn("Potential realloc error, could not find matching allocation for original address: %d", pOriginal);
                    new Exception().printStackTrace();
                }

                final var realloc = MemoryUtil.nmemRealloc(pOriginal, size);
                if ((realloc & (alignment - 1)) == 0) {
                    allocations.put(realloc, obj);
                    return realloc;
                }
                final var newAlignedAlloc = MemoryUtil.nmemAlignedAlloc(alignment, size);
                MemoryUtil.memCopy(realloc, newAlignedAlloc, size);
                MemoryUtil.nmemFree(realloc);
                allocations.put(newAlignedAlloc, obj);
                return newAlignedAlloc;
            }
        });

        event.callbacks = pAlloc;
    }

    @Override
    public void free() {
        alloc.close();
    }

    public record Allocation(long address, StackTraceElement[] stackTrace) {}

}
