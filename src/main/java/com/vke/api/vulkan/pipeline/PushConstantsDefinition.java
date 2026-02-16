package com.vke.api.vulkan.pipeline;

import com.vke.api.utils.AlignedByteBuffer;
import com.vke.core.vulkan.shader.Shader;

import java.nio.ByteBuffer;

public abstract class PushConstantsDefinition {
    public static final int ALIGN = 16;


    protected abstract int size();
    public abstract int getOffset();
    public abstract Shader.Stages getAplicableStages();
    public abstract ByteBuffer getBytes(AlignedByteBuffer buf);

    protected int t_float() { return 4; }
    protected int t_ptr() { return 8; }
    protected int t_mat2() { return 16; }
    protected int t_mat3() { return 36; }
    protected int t_mat4() { return 64; }
    protected int t_vec2() { return 8; }
    protected int t_vec3() { return 12; }
    protected int t_vec4() { return 16; }

    public int getSize(int minAlign) {
        int s = size();
        return ((s + minAlign - 1) / minAlign) * minAlign;
    }

    protected int alignCorrectly(int size) {
        int minAlign = ALIGN;
        return ((size + minAlign - 1) / minAlign) * minAlign;
    }
}
