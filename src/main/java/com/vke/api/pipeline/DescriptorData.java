package com.vke.api.pipeline;

import com.carrotsearch.hppc.IntObjectHashMap;
import com.vke.api.abstraction.IntEnum;
import com.vke.api.pipeline.handles.UniformHandle;
import com.vke.core.vulkan.shader.VulkanShader;
import com.vke.utils.Disposable;
import com.vke.utils.Utils;
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

    public static class Binding {

        public String name;
        public Type type;
        public VulkanShader.Stages stages;
        public Struct struct;
        public long buf;
        public long gpuBuf;
        public int textureCount;

        public Binding(Binding b, VulkanShader.Stages stages, long buf, long gpuBuf) {
            this(b.name, b.type, stages, b.struct, buf, gpuBuf, b.textureCount);
        }

        public Binding(String name, Type type, Struct struct, int textureCount) {
            this(name, type, null, struct, 0, 0, textureCount);
        }

        public Binding(String name, Type type, VulkanShader.Stages stages, Struct struct, long buf, long gpuBuf, int textureCount) {
            this.name = name;
            this.type = type;
            this.stages = stages;
            this.struct = struct;
            this.buf = buf;
            this.gpuBuf = gpuBuf;
            this.textureCount = textureCount;
        }

        public boolean compare(Binding b) {
            return name.equals(b.name) && type == b.type && struct.equals(b.struct);
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

}
