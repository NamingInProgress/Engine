package com.vke.api.abstraction.descriptors.buffer;

import com.vke.api.abstraction.IntBitEnum;
import com.vke.api.abstraction.IntEnum;
import org.lwjgl.util.vma.Vma;

public class MemoryUsage implements IntBitEnum<MemoryUsage, MemoryUsage.Bits> {
    private int mask;

    public MemoryUsage(Bits... bits) {
        or(bits);
    }

    @Override
    public MemoryUsage or(Bits... flags) {
        for (Bits bit : flags) {
            mask |= bit.getVkHandle();
        }
        return this;
    }

    @Override
    public int getVkHandle() {
        return mask;
    }

    public enum Bits implements IntEnum {
        UNKNOWN(Vma.VMA_MEMORY_USAGE_UNKNOWN),
        GPU_ONLY(Vma.VMA_MEMORY_USAGE_GPU_ONLY),
        CPU_ONLY(Vma.VMA_MEMORY_USAGE_CPU_ONLY),
        CPU_TO_GPU(Vma.VMA_MEMORY_USAGE_CPU_TO_GPU),
        GPU_TO_CPU(Vma.VMA_MEMORY_USAGE_GPU_TO_CPU),
        CPU_COPY(Vma.VMA_MEMORY_USAGE_CPU_COPY),
        GPU_LAZILY_ALLOCATED(Vma.VMA_MEMORY_USAGE_GPU_LAZILY_ALLOCATED),
        AUTO(Vma.VMA_MEMORY_USAGE_AUTO),
        AUTO_PREFER_DEVICE(Vma.VMA_MEMORY_USAGE_AUTO_PREFER_DEVICE),
        AUTO_PREFER_HOST(Vma.VMA_MEMORY_USAGE_AUTO_PREFER_HOST);

        private final int vkHandle;

        Bits(int vkHandle) {
            this.vkHandle = vkHandle;
        }

        @Override
        public int getVkHandle() {
            return vkHandle;
        }

        public MemoryUsage into() {
            return new MemoryUsage(this);
        }
    }
}
