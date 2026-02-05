package com.vke.test;

import com.vke.api.vulkan.pipeline.PushConstantsDefinition;
import com.vke.core.rendering.vulkan.shader.Shader;
import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;

public class TestPushConstant extends PushConstantsDefinition {
    private long verticesPtr;

    public void setVerticesPtr(long ptr) {
        this.verticesPtr = ptr;
    }

    @Override
    public int getSize() {
        return 8;
    }

    @Override
    public int getOffset() {
        return 0;
    }

    @Override
    public Shader.Stages getAplicableStages() {
        return new Shader.Stages(Shader.Type.VERTEX);
    }

    @Override
    public ByteBuffer getBytes(MemoryStack stack) {
        return stack.calloc(getSize()).putLong(verticesPtr).flip();
    }
}
