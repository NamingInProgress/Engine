package com.vke.core.vulkan.buffers.premade.slice;

import com.vke.api.rendering.abstraction.enums.buffer.PackingType;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

public class PushConstantBufferSlice extends BufferSlice {
    public PushConstantBufferSlice(long bufferAddress, long offset, int length, PackingType packingType) {
        super(bufferAddress, offset, length, packingType);
    }

    @Override
    public void putFloat3(float x, float y, float z) {
        putFloat4(x, y, z, 0f);
    }

    @Override
    public void putMat4(Matrix4f m) {
        putFloat4(m.m00(), m.m10(), m.m20(), m.m30());
        putFloat4(m.m01(), m.m11(), m.m21(), m.m31());
        putFloat4(m.m02(), m.m12(), m.m22(), m.m32());
        putFloat4(m.m03(), m.m13(), m.m23(), m.m33());
    }

    @Override
    public void putMat3(Matrix3f m) {
        putFloat3(m.m00(), m.m10(), m.m20());
        putFloat3(m.m01(), m.m11(), m.m21());
        putFloat3(m.m02(), m.m12(), m.m22());
    }
}
