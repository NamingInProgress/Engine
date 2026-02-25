package com.vke.test.app;

import com.vke.api.utils.AlignedByteBuffer;
import com.vke.api.vulkan.pipeline.PushConstantsDefinition;
import com.vke.core.vulkan.shader.VulkanShader;
import com.vke.api.abstraction.descriptors.ShaderType;
import org.joml.Matrix4f;

import java.nio.ByteBuffer;

public class TestPushConstant extends PushConstantsDefinition {
    private long verticesPtr;
    private Matrix4f mat;

    public void setVerticesPtr(long ptr) {
        this.verticesPtr = ptr;
    }
    public void setMat(Matrix4f mat) { this.mat = mat; }

    @Override
    public int size() {
        return alignCorrectly(t_ptr() + t_mat4());
    }

    @Override
    public int getOffset() {
        return 0;
    }

    @Override
    public VulkanShader.Stages getAplicableStages() {
        return new VulkanShader.Stages(ShaderType.VERTEX);
    }

    @Override
    public ByteBuffer getBytes(AlignedByteBuffer buf) {
        buf.long1(verticesPtr);
        buf.float4x4(mat);

        return buf.getBuffer().flip();
    }
}
