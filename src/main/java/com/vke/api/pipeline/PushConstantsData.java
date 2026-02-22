package com.vke.api.pipeline;

import com.vke.api.abstraction.descriptors.buffer.PackingType;
import com.vke.api.pipeline.handles.PushConstantHandle;
import com.vke.core.vulkan.shader.Shader;

import java.nio.ByteBuffer;
import java.util.HashMap;

public abstract class PushConstantsData {

    protected HashMap<String, PushConstantHandle> HANDLE_CACHE = new HashMap<>();
    protected HashMap<String, PushConstant> pushConstants = new HashMap<>();

    // name -> bufferName.entryName
    public PushConstantHandle resolve(String name) {
        if (HANDLE_CACHE.containsKey(name)) return HANDLE_CACHE.get(name);
        String[] split = name.split("\\.");
        PushConstantHandle handle = new PushConstantHandle();

        PushConstant pc = pushConstants.get(split[0]);
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
        protected ByteBuffer buffer;
        protected PackingType packing;

        public Entry getEntry(String name) {
            return struct.entries.get(name);
        }

    }

}
