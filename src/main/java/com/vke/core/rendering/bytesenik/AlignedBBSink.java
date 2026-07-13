package com.vke.core.rendering.bytesenik;

import com.vke.api.rendering.vulkan.buffer.VertexEcoder;
import com.vke.api.utils.AlignedByteBuffer;

import java.util.Objects;

public class AlignedBBSink implements VertexEcoder {
    private final AlignedByteBuffer abb;

    public AlignedBBSink(AlignedByteBuffer abb) {
        Objects.requireNonNull(abb);
        this.abb = abb;
    }

    @Override
    public void float1(float x) {
        abb.float1(x);
    }

    @Override
    public void float2(float x, float y) {
        abb.float2(x, y);
    }

    @Override
    public void float3(float x, float y, float z) {
        abb.float3(x, y, z);
    }

    @Override
    public void float4(float x, float y, float z, float w) {
        abb.float4(x, y, z, w);
    }

    @Override
    public void int2(int x, int y) {
        abb.int2(x, y);
    }

    @Override
    public void int3(int x, int y, int z) {
        abb.int3(x, y, z);
    }

    @Override
    public void int4(int x, int y, int z, int w) {
        abb.int4(x, y, z, w);
    }

    @Override
    public void uint2(int x, int y) {
        abb.int2(x, y);
    }

    @Override
    public void uint3(int x, int y, int z) {
        abb.int3(x, y, z);
    }

    @Override
    public void uint4(int x, int y, int z, int w) {
        abb.int4(x, y, z, w);
    }

    @Override
    public void double2(double x, double y) {
        abb.double2(x, y);
    }

    @Override
    public void double3(double x, double y, double z) {
        abb.double3(x, y, z);
    }

    @Override
    public void double4(double x, double y, double z, double w) {
        abb.double4(x, y, z, w);
    }

    @Override
    public void int1(int x) {
        abb.int1(x);
    }

    @Override
    public void uint1(int x) {
        abb.int1(x);
    }

    @Override
    public void double1(double x) {
        abb.double1(x);
    }
}
