package com.vke.api.rendering.vulkan.pushconstants;

import com.vke.api.rendering.vulkan.descriptors.parsing.HandleParser;
import com.vke.api.rendering.vulkan.descriptors.parsing.LayoutResolver;
import com.vke.api.rendering.vulkan.descriptors.parsing.node.EntryNode;
import com.vke.core.memory.AutoHeapAllocator;
import com.vke.utils.io.Disposable;
import org.jetbrains.annotations.ApiStatus;
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

    private long pipelineLayoutHandle;

    public PushConstants(PushConstantLayout layout) {
        this.layout = layout;

        this.data = alloc.allocByteBuffer(align16((int) layout.size)).getHeapObject();
    }

    public static int align16(int value) {
        int alignment = 16;
        return ((value + alignment - 1) / alignment) * alignment;
    }

    @ApiStatus.Internal
    public void setHandle(long pipelineLayoutHandle) {
        this.pipelineLayoutHandle = pipelineLayoutHandle;
    }

    public PushConstantHandle resolve(String name) {
        if (HANDLE_CACHE.containsKey(name)) return HANDLE_CACHE.get(name);

        PushConstantHandle handle = createHandle(name);
        if (handle == null) return null;
        HANDLE_CACHE.put(name, handle);
        return handle;
    }

    public ByteBuffer getData() { return this.data; }

    public PushConstantHandle createHandle(String name) {
        EntryNode root = (EntryNode) parser.parse(name).child;

        LayoutResolver.LayoutResolution res = resolver.resolveLayoutPath(layout.typeLayout, root);

        return new PushConstantHandle(pipelineLayoutHandle, MemoryUtil.memAddress(data), res.finalType().size, res.offset());
    }

    public PushConstantLayout getLayout() { return this.layout; }

    @Override
    public void free() {
        alloc.close();
    }
}
