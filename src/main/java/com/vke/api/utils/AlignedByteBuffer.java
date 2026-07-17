package com.vke.api.utils;

import org.joml.*;

import java.lang.Math;
import java.nio.ByteBuffer;

public class AlignedByteBuffer {
    private final ByteBuffer buf;
    private final int minAlign;

    public AlignedByteBuffer(ByteBuffer buf, int minAlign) {
        this.buf = buf;
        this.minAlign = minAlign;
    }

    public ByteBuffer getBuffer() {
        return buf;
    }

    private void align(int sizeUsed) {
        int toFill = (minAlign - (sizeUsed % minAlign)) % minAlign;
        if (toFill > 0) {
            toFill = Math.min(toFill, buf.remaining());
            buf.put(new byte[toFill]);
        }
    }


    public void u8(byte b) {
        buf.put(b);
        align(1);
    }

    // -------------------- Scalars --------------------
    public void float1(float v) {
        buf.putFloat(v);
        align(4);
    }

    public void int1(int v) {
        buf.putInt(v);
        align(4);
    }

    public void long1(long v) {
        buf.putLong(v);
        align(8);
    }

    // -------------------- Vectors --------------------
    public void float2(Vector2f v) {
        buf.putFloat(v.x).putFloat(v.y);
        align(8);
    }

    public void float2(float v1, float v2) {
        buf.putFloat(v1).putFloat(v2);
        align(8);
    }

    public void float3(Vector3f v) {
        buf.putFloat(v.x).putFloat(v.y).putFloat(v.z);
        align(12);
    }

    public void float3(float v1, float v2, float v3) {
        buf.putFloat(v1).putFloat(v2).putFloat(v3);
        align(12);
    }

    public void float4(Vector4f v) {
        buf.putFloat(v.x).putFloat(v.y).putFloat(v.z).putFloat(v.w);
        align(16);
    }

    public void float4(float v1, float v2, float v3, float v4) {
        buf.putFloat(v1).putFloat(v2).putFloat(v3).putFloat(v4);
        align(16);
    }

    public void int2(Vector2i v) {
        buf.putInt(v.x).putInt(v.y);
        align(8);
    }

    public void int2(int x, int y) {
        buf.putInt(x).putInt(y);
        align(8);
    }

    public void int3(Vector3i v) {
        buf.putInt(v.x).putInt(v.y).putInt(v.z);
        align(12);
    }

    public void int3(int x, int y, int z) {
        buf.putInt(x).putInt(y).putInt(z);
        align(12);
    }

    public void int4(Vector4i v) {
        buf.putInt(v.x).putInt(v.y).putInt(v.z).putInt(v.w);
        align(16);
    }

    public void int4(int x, int y, int z, int w) {
        buf.putInt(x).putInt(y).putInt(z).putInt(w);
        align(16);
    }

    // -------------------- Matrices (column-major) --------------------
    public void float2x2(Matrix2f m) {
        for (int c = 0; c < 2; c++)
            for (int r = 0; r < 2; r++)
                buf.putFloat(m.get(r, c));
        align(16);
    }

    public void float3x3_std140(Matrix3f m) {
        for (int c = 0; c < 3; c++) {
            buf.putFloat(m.get(0, c));
            buf.putFloat(m.get(1, c));
            buf.putFloat(m.get(2, c));
            buf.putFloat(0f);
        }
        align(3*16);
    }

    public void float3x3(Matrix3f m) {
        for (int c = 0; c < 3; c++)
            for (int r = 0; r < 3; r++)
                buf.putFloat(m.get(r, c));
        align(3*3*4);
    }

    public void float4x4(Matrix4f m) {
        for (int c = 0; c < 4; c++)
            for (int r = 0; r < 4; r++)
                buf.putFloat(m.get(c, r));
        align(64);
    }

    public void double1(double v) {
        buf.putDouble(v);
        align(8);
    }

    public void double2(double v1, double v2) {
        buf.putDouble(v1).putDouble(v2);
        align(16);
    }

    public void double3(double v1, double v2, double v3) {
        buf.putDouble(v1).putDouble(v2).putDouble(v3);
        align(24);
    }

    public void double4(double v1, double v2, double v3, double v4) {
        buf.putDouble(v1).putDouble(v2).putDouble(v3).putDouble(v4);
        align(32);
    }
}
