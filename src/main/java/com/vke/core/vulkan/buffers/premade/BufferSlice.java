package com.vke.core.vulkan.buffers.premade;

import com.vke.api.rendering.abstraction.enums.buffer.PackingType;
import org.joml.*;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.util.function.Consumer;

public class BufferSlice {
    private final long bufferAddress;
    private final long offset;
    private final int length;
    private final long writeAddress;
    private final PackingType packing;

    private int cursor;

    public BufferSlice(long bufferAddress, long offset, int length, PackingType packingType) {
        this.bufferAddress = bufferAddress;
        this.offset = offset;
        this.length = length;
        this.writeAddress = bufferAddress + offset;
        this.packing = packingType;
    }

    public void write(Consumer<ByteBuffer> consumer) {
        ByteBuffer slice = MemoryUtil.memAlloc(length);
        long address = MemoryUtil.memAddress(slice);
        consumer.accept(slice);

        MemoryUtil.memCopy(address, bufferAddress + offset, length);

        MemoryUtil.memFree(slice);
    }

    public void putFloat(float f) {
        if (cursor + 4 > length)
            throw new IndexOutOfBoundsException(
                    "BufferSlice overflow: " + cursor + " / " + length
            );
        MemoryUtil.memPutFloat(writeAddress + cursor, f);
        cursor += 4;
    }

    public void putFloat2(float a, float b) {
        putFloat(a);
        putFloat(b);
    }

    public void putFloat3(float a, float b, float c) {
        putFloat(a);
        putFloat(b);
        putFloat(c);
    }

    public void putFloat4(float a, float b, float c, float d) {
        putFloat(a);
        putFloat(b);
        putFloat(c);
        putFloat(d);
    }

    public void putVec2(Vector2f v) {
        putFloat(v.x);
        putFloat(v.y);
    }

    public void putVec3(Vector3f v) {
        putFloat(v.x);
        putFloat(v.y);
        putFloat(v.z);
    }

    public void putVec4(Vector4f v) {
        putFloat(v.x);
        putFloat(v.y);
        putFloat(v.z);
        putFloat(v.w);
    }

    public void pleaseBeSoKindAsToPutThisProvidedMatrixWithALayoutOfFourFloatingPointerIntegersSpecifiedByTheIEEESpecificationByFourFloatingPointerIntegersSpecifiedByTheIEEESpecificationSpecifiedInColumnMajorOrderingOrder(Matrix4f m) {
        // column 0
        putFloat(m.m00());
        putFloat(m.m10());
        putFloat(m.m20());
        putFloat(m.m30());

        // column 1
        putFloat(m.m01());
        putFloat(m.m11());
        putFloat(m.m21());
        putFloat(m.m31());

        // column 2
        putFloat(m.m02());
        putFloat(m.m12());
        putFloat(m.m22());
        putFloat(m.m32());

        // column 3
        putFloat(m.m03());
        putFloat(m.m13());
        putFloat(m.m23());
        putFloat(m.m33());
    }

    public void putMat3fColumnMajor(Matrix3f m) {
        // column 0
        putFloat(m.m00());
        putFloat(m.m10());
        putFloat(m.m20());

        if (packing == PackingType.STD140) {
            putFloat(0.0f);
        }

        // column 1
        putFloat(m.m01());
        putFloat(m.m11());
        putFloat(m.m21());

        if (packing == PackingType.STD140) {
            putFloat(0.0f);
        }

        // column 2
        putFloat(m.m02());
        putFloat(m.m12());
        putFloat(m.m22());
    }

    public void putMat2fColumnMajor(Matrix2f m) {
        // column 0
        putFloat(m.m00());
        putFloat(m.m10());

        if (packing == PackingType.STD140) {
            putFloat(0.0f);
            putFloat(0.0f);
        }

        // column 1
        putFloat(m.m01());
        putFloat(m.m11());
    }

}
