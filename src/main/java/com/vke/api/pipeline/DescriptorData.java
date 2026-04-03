package com.vke.api.pipeline;

import com.carrotsearch.hppc.IntObjectHashMap;
import com.vke.api.rendering.abstraction.IntEnum;
import com.vke.api.rendering.vulkan.descriptors.handles.UniformHandle;
import com.vke.core.vulkan.shader.VulkanShader;
import com.vke.utils.io.Disposable;
import com.vke.utils.Utils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.vulkan.VK14;

import java.util.Arrays;
import java.util.HashMap;

public abstract class DescriptorData implements Disposable {

    protected final HashMap<String, UniformHandle> HANDLE_CACHE = new HashMap<>();
    protected final IntObjectHashMap<Set> sets = new IntObjectHashMap<>();

    public static class Set {

        public int set;
        public final IntObjectHashMap<Binding> bindings = new IntObjectHashMap<>();

        public int getSet() { return this.set; }

    }

    // name -> bindingName.entryName
    public UniformHandle resolve(String name) {
        if (HANDLE_CACHE.containsKey(name)) return HANDLE_CACHE.get(name);

        UniformHandle handle = createHandle(name);
        if (handle == null) return null;
        HANDLE_CACHE.put(name, handle);
        return handle;
    }

    protected abstract UniformHandle createHandle(String name);

    public abstract static class Binding {

        public final String name;
        public final Type type;
        public final VulkanShader.Stages stages;
        public final @Nullable Struct struct; // STRUCT'S SIZE MUST ABSOLUTELY MUSTTT BE BYTE ALIGNED TO WHATEVER THE BUFFER REQUIRES AND SHIT
        public final int binding;
        public final int descriptorCount; // -1 if it's not an array

        public Binding(Binding b, VulkanShader.Stages stages, int descriptorCount) {
            this(b.name, b.binding, b.type, stages, b.struct, descriptorCount);
        }

        public Binding(String name, int binding, Type type, @Nullable Struct struct, int descriptorCount) {
            this(name, binding, type, null, struct, descriptorCount);
        }

        public Binding(String name, int binding, Type type, VulkanShader.Stages stages, @Nullable Struct struct, int descriptorCount) {
            this.name = name;
            this.type = type;
            this.stages = stages;
            this.struct = struct;
            this.binding = binding;
            this.descriptorCount = descriptorCount;
        }

        public boolean compare(Binding b) {
            boolean structEquals = struct == null || struct.equals(b.struct);
            return name.equals(b.name) && type == b.type && structEquals;
        }

        public enum Type implements IntEnum {

            COMBINED_IMAGE_SAMPLER(VK14.VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER, "combined_image_sampler", "cis"),
            STORAGE_IMAGE(VK14.VK_DESCRIPTOR_TYPE_STORAGE_IMAGE, "storage_image", "si"),
            UNIFORM_BUFFER(VK14.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER, "uniform_buffer", "UBO"),
            STORAGE_BUFFER(VK14.VK_DESCRIPTOR_TYPE_STORAGE_BUFFER, "storage_buffer", "SSBO");

            private final int vkHandle;
            private final String[] names;

            Type(int vkHandle, String... names) {
                this.vkHandle = vkHandle;
                this.names = names;
            }

            public String[] getNames() { return this.names; }

            public boolean isBuffer() { return this == UNIFORM_BUFFER || this == STORAGE_BUFFER; }

            public static Type fromString(String name) {
                return Arrays.stream(Type.values()).filter(c -> Utils.arrayContains(c.getNames(), name)).findFirst().orElse(Type.valueOf(name));
            }

            @Override
            public int getVkHandle() {
                return vkHandle;
            }

        }

    }

    public static class BufferBinding extends Binding {
        public final long buf;
        public final long gpuBuf;
        public final int totalSize;

        public BufferBinding(BufferBinding b, VulkanShader.Stages stages, long cpuBuffer, long gpuBuffer, int arraySize, int totalSize) {
            this(b.name, b.binding, b.type, stages, b.struct, cpuBuffer, gpuBuffer, arraySize, totalSize);
        }

        public BufferBinding(String name, int binding, Type type, @NotNull Struct struct, int arraySize, int totalSize) {
            this(name, binding, type, null, struct, 0, 0, arraySize, totalSize);
        }

        public BufferBinding(String name, int binding, Type type, VulkanShader.Stages stages, @NotNull Struct struct, long cpuBuffer, long gpuBuffer, int arraySize, int totalSize) {
            super(name, binding, type, stages, struct, arraySize);
            this.buf = cpuBuffer;
            this.gpuBuf = gpuBuffer;
            this.totalSize = totalSize;
        }
    }

    public static class ImageBinding extends Binding {
        public ImageBinding(Binding b, VulkanShader.Stages stages, int arraySize) {
            this(b.name, b.binding, b.type, stages, b.struct, arraySize);
        }

        public ImageBinding(String name, int binding, Type type, @Nullable Struct struct, int arraySize) {
            this(name, binding, type, null, struct, arraySize);
        }

        public ImageBinding(String name, int binding, Type type, VulkanShader.Stages stages, @Nullable Struct struct, int arraySize) {
            super(name, binding, type, stages, struct, arraySize);
        }
    }

}
