package com.vke.core.rendering.vulkan.descriptor;

import com.vke.core.rendering.vulkan.buffers.MappedBuffer;

import java.util.HashMap;

public class SharedBufferHandler {

    public static final HashMap<String, MappedBuffer> BUFFERS = new HashMap<>();

    static {
        requestSharedBuffer("u_MaterialBuffer");
        requestSharedBuffer("u_LightsBuffer");
    }

    public static void requestSharedBuffer(String name) {
        BUFFERS.put(name, null);
    }

}
