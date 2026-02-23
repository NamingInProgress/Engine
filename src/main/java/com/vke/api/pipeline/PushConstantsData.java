package com.vke.api.pipeline;

import com.vke.api.abstraction.descriptors.buffer.PackingType;
import com.vke.api.pipeline.handles.PushConstantHandle;
import com.vke.core.vulkan.shader.Shader;
import com.vke.utils.Disposable;

import java.nio.ByteBuffer;
import java.util.HashMap;

public abstract class PushConstantsData implements Disposable {

    protected HashMap<String, PushConstantHandle> HANDLE_CACHE = new HashMap<>();
    protected HashMap<String, PushConstant> pushConstants = new HashMap<>();

    // name -> bufferName.entryName
    public PushConstantHandle resolve(String name) {
        if (HANDLE_CACHE.containsKey(name)) return HANDLE_CACHE.get(name);
        String[] split = name.split("\\.");
        PushConstantHandle handle = new PushConstantHandle();

        PushConstant pc = pushConstants.get(split[0]);
        if (pc == null) return null;
        Entry e = pc.getEntry(split[1]);
        handle.buffer = pc.buffer;
        handle.offset = e.offset;
        handle.size = e.size;
        handle.packing = pc.packing;

        HANDLE_CACHE.put(name, handle);
        return handle;
    }

    public static class PushConstant {

        protected String name;
        protected Shader.Stages shaderStages;
        protected Struct struct;
        protected long buffer;
        protected PackingType packing;

        public PushConstant(PushConstant base, Shader.Stages stages, long buf) {
            this(base.name, stages, base.struct, buf, base.packing);
        }

        public PushConstant(String name, Struct struct, PackingType packing) {
            this(name, null, struct, 0, packing);
        }

        public PushConstant(String name, Shader.Stages stages, Struct struct, long buf, PackingType packing) {
            this.name = name;
            this.struct = struct;
            this.packing = packing;
            this.shaderStages = stages;
            this.buffer = buf;
        }

        public String getName() { return this.name; }

        public Entry getEntry(String name) {
            return struct.entries.get(name);
        }

        public int sizeof() { return struct.sizeof(); }

        public boolean compare(PushConstant o) {
            return name.equals(o.name) && struct.equals(o.struct) && packing == o.packing;
        }

    }

}
