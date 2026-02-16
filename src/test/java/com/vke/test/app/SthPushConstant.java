package com.vke.test.app;

import com.vke.api.utils.AlignedByteBuffer;
import com.vke.api.vulkan.pipeline.PushConstantsDefinition;
import com.vke.core.vulkan.shader.Shader;
import com.vke.api.abstraction.descriptors.ShaderType;

import java.nio.ByteBuffer;

public class SthPushConstant extends PushConstantsDefinition {

    private long verticesPtr;
    public void setVerticesPtr(long ptr) {
        this.verticesPtr = ptr;
    }

    @Override
    protected int size() {
        return alignCorrectly(t_ptr());
    }

    @Override
    public int getOffset() {
        return 0;
    }

    @Override
    public Shader.Stages getAplicableStages() {
        return new Shader.Stages(ShaderType.VERTEX);
    }

    @Override
    public ByteBuffer getBytes(AlignedByteBuffer buf) {
        buf.long1(verticesPtr);

        return buf.getBuffer().flip();
    }
}
