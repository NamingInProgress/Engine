package com.vke.api.rendering.vulkan.pushconstants;

import com.vke.api.rendering.vulkan.descriptors.handles.parsing.HandleParser;
import com.vke.api.rendering.vulkan.descriptors.handles.parsing.LayoutResolver;
import com.vke.api.rendering.vulkan.descriptors.handles.parsing.node.EntryNode;
import com.vke.core.memory.AutoHeapAllocator;
import com.vke.utils.Disposable;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.util.HashMap;

public class PushConstants implements Disposable {

    private final HashMap<String, PushConstantHandle> HANDLE_CACHE = new HashMap<>();

    private final PushConstantLayout layout;
    private final ByteBuffer data;
    private final AutoHeapAllocator alloc = new AutoHeapAllocator();
    private final HandleParser parser = new HandleParser();
    private final LayoutResolver resolver = new LayoutResolver();
    private final long pipelineLayoutHandle;

    public PushConstants(PushConstantLayout layout, long pipelineLayoutHandle) {
        this.layout = layout;
        this.pipelineLayoutHandle = pipelineLayoutHandle;

        this.data = alloc.allocByteBuffer((int) layout.size).getHeapObject();
    }

    public PushConstantHandle resolve(String name) {
        if (HANDLE_CACHE.containsKey(name)) return HANDLE_CACHE.get(name);

        PushConstantHandle handle = createHandle(name);
        if (handle == null) return null;
        HANDLE_CACHE.put(name, handle);
        return handle;
    }

    public PushConstantHandle createHandle(String name) {
        EntryNode root = (EntryNode) parser.parse(name).child;

        LayoutResolver.LayoutResolution res = resolver.resolveLayoutPath(layout.typeLayout, root);

        return new PushConstantHandle(pipelineLayoutHandle, MemoryUtil.memAddress(data), res.finalType().size, res.offset());
    }

    @Override
    public void free() {
        alloc.close();
    }
}
