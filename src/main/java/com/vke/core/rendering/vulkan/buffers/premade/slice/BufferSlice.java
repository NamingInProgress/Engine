package com.vke.core.rendering.vulkan.buffers.premade.slice;

import com.vke.api.rendering.abstraction.renderer.RenderSystem;
import com.vke.api.rendering.abstraction.renderer.data.Texture;
import com.vke.api.rendering.abstraction.renderer.enums.buffer.PackingType;
import org.joml.*;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;

public class BufferSlice {
    protected final long bufferAddress;
    protected final long offset;
    protected final int length;
    protected final long writeAddress;
    protected final PackingType packing;

    private int cursor;

    public BufferSlice(long bufferAddress, long offset, int length, PackingType packingType) {
        this.bufferAddress = bufferAddress;
        this.offset = offset;
        this.length = length;
        this.writeAddress = bufferAddress + offset;
        this.packing = packingType;
    }

    public void skip(int amount) {
        if (cursor + amount > length)
            throw new IndexOutOfBoundsException(
                    "BufferSlice overflow: " + cursor + " / " + length
            );
        cursor += amount;
    }

    public void putData(ByteBuffer buf) {
        if (cursor + buf.remaining() > length)
            throw new IndexOutOfBoundsException(
                    "BufferSlice overflow: " + cursor + " / " + length
            );
        MemoryUtil.memCopy(MemoryUtil.memAddress(buf), writeAddress + cursor, buf.remaining());
        cursor += buf.remaining();
    }

    public void putIntAt(int byteOffset, int val) {
        if (byteOffset + 4 > length)
            throw new IndexOutOfBoundsException(
                    "BufferSlice overflow: " + byteOffset + " / " + length
            );
        MemoryUtil.memPutInt(writeAddress + byteOffset, val);
    }

    public void putSampler(RenderSystem sys, Texture tex) {
        putInt(sys.textureManager().texture(tex));
    }

    public void putInt(int i) {
        if (cursor + 4 > length)
            throw new IndexOutOfBoundsException(
                    "BufferSlice overflow: " + cursor + " / " + length
            );
        MemoryUtil.memPutInt(writeAddress + cursor, i);
        cursor += 4;
    }

    public void putFloat(float f) {
        if (cursor + 4 > length)
            throw new IndexOutOfBoundsException(
                    "BufferSlice overflow: " + cursor + " / " + length
            );
        MemoryUtil.memPutFloat(writeAddress + cursor, f);
        cursor += 4;
    }

    public void putLong(long l) {
        if (cursor + 8 > length) throw new IndexOutOfBoundsException(
                "BufferSlice overflow: " + cursor + " / " + length
        );
        MemoryUtil.memPutLong(writeAddress + cursor, l);
        cursor += 8;
    }

    public void putFloat2(float a, float b) {
        putFloat(a);
        putFloat(b);
    }

    public void putFloat3(float a, float b, float c) {
        putFloat(a);
        putFloat(b);
        putFloat(c);
        if (packing == PackingType.STD140) putFloat(0.0f);
    }

    public void putFloat4(float a, float b, float c, float d) {
        putFloat(a);
        putFloat(b);
        putFloat(c);
        putFloat(d);
    }

    public void putVec2(Vector2f v) {
        putFloat2(v.x, v.y);
    }

    public void putVec3(Vector3f v) {
        putFloat3(v.x, v.y, v.z);
    }

    public void putVec4(Vector4f v) {
        putFloat4(v.x, v.y, v.z, v.w);
    }

    public void putMat4(Matrix4f m) {
        putFloat(m.m00());
        putFloat(m.m01());
        putFloat(m.m02());
        putFloat(m.m03());

        // Column 1
        putFloat(m.m10());
        putFloat(m.m11());
        putFloat(m.m12());
        putFloat(m.m13());

        // Column 2
        putFloat(m.m20());
        putFloat(m.m21());
        putFloat(m.m22());
        putFloat(m.m23());

        // Column 3
        putFloat(m.m30());
        putFloat(m.m31());
        putFloat(m.m32());
        putFloat(m.m33());
    }

    public void putMat3(Matrix3f m) {
        // column 0
        putFloat(m.m00());
        putFloat(m.m01());
        putFloat(m.m02());

        if (packing == PackingType.STD140) {
            putFloat(0.0f);
        }

        // column 1
        putFloat(m.m10());
        putFloat(m.m11());
        putFloat(m.m12());

        if (packing == PackingType.STD140) {
            putFloat(0.0f);
        }

        // column 2
        putFloat(m.m20());
        putFloat(m.m21());
        putFloat(m.m22());
    }

    public void putMat2(Matrix2f m) {
        // column 0
        putFloat(m.m00());
        putFloat(m.m01());
        // column 1
        putFloat(m.m10());
        putFloat(m.m11());
    }

}
