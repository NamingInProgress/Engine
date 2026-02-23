package com.vke.core.vulkan.pipeline;

import com.vke.api.pipeline.PushConstantsData;
import com.vke.core.memory.AutoHeapAllocator;
import com.vke.core.vulkan.shader.Shader;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;

public class VulkanPushConstants extends PushConstantsData {

    private final AutoHeapAllocator alloc;

    public VulkanPushConstants() {
        this.alloc = new AutoHeapAllocator();
    }

    public PushConstant initAndAddPushConstant(PushConstant c, Shader.Stages stages) {
        ByteBuffer buf = alloc.alloc(c.sizeof()).getHeapObject();
        PushConstant pc = new PushConstant(c, stages, MemoryUtil.memAddress(buf));

        this.pushConstants.put(pc.getName(), pc);
        return pc;
    }

    @Override
    public void free() {
        alloc.close();
    }

}
