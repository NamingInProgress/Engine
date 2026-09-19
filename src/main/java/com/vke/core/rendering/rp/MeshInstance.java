package com.vke.core.rendering.rp;

import com.vke.core.rendering.vulkan.buffers.premade.slice.BufferSlice;
import org.joml.Matrix4f;

public record MeshInstance(Matrix4f mat, int materialIndex) {

    public void putSelf(BufferSlice writer) {
        writer.mat4(mat);
        writer.int1(materialIndex);
    }

}
