package com.vke.api.rendering.abstraction.renderer.data;

import org.joml.*;

public interface ByteEncoder {
    // float
    void float1(float x);

    default void float2(float x, float y) {
        float1(x);
        float1(y);
    }
    default void float2(Vector2f v) {
        float2(v.x, v.y);
    }
    default void float2(float[] v) {
        float2(v[0], v[1]);
    }

    default void float3(float x, float y, float z) {
        float1(x);
        float1(y);
        float1(z);
    }
    default void float3(Vector3f v) {
        float3(v.x, v.y, v.z);
    }
    default void float3(float[] v) {
        float3(v[0], v[1], v[2]);
    }

    default void float4(float x, float y, float z, float w) {
        float1(x);
        float1(y);
        float1(z);
        float1(w);
    }
    default void float4(Vector4f v) {
        float4(v.x, v.y, v.z, v.w);
    }
    default void float4(float[] v) {
        float4(v[0], v[1], v[2], v[3]);
    }


    // int
    void int1(int x);

    default void int2(int x, int y) {
        int1(x);
        int1(y);
    }
    default void int2(Vector2i v) {
        int2(v.x, v.y);
    }
    default void int2(int[] v) {
        int2(v[0], v[1]);
    }

    default void int3(int x, int y, int z) {
        int1(x);
        int1(y);
        int1(z);
    }
    default void int3(Vector3i v) {
        int3(v.x, v.y, v.z);
    }
    default void int3(int[] v) {
        int3(v[0], v[1], v[2]);
    }

    default void int4(int x, int y, int z, int w) {
        int1(x);
        int1(y);
        int1(z);
        int1(w);
    }
    default void int4(Vector4i v) {
        int4(v.x, v.y, v.z, v.w);
    }
    default void int4(int[] v) {
        int4(v[0], v[1], v[2], v[3]);
    }


    // uint
    void uint1(int x);

    default void uint2(int x, int y) {
        uint1(x);
        uint1(y);
    }
    default void uint2(Vector2i v) {
        uint2(v.x, v.y);
    }
    default void uint2(int[] v) {
        uint2(v[0], v[1]);
    }

    default void uint3(int x, int y, int z) {
        uint1(x);
        uint1(y);
        uint1(z);
    }
    default void uint3(Vector3i v) {
        uint3(v.x, v.y, v.z);
    }
    default void uint3(int[] v) {
        uint3(v[0], v[1], v[2]);
    }

    default void uint4(int x, int y, int z, int w) {
        uint1(x);
        uint1(y);
        uint1(z);
        uint1(w);
    }
    default void uint4(Vector4i v) {
        uint4(v.x, v.y, v.z, v.w);
    }
    default void uint4(int[] v) {
        uint4(v[0], v[1], v[2], v[3]);
    }


    // double
    void double1(double x);

    default void double2(double x, double y) {
        double1(x);
        double1(y);
    }
    default void double2(Vector2d v) {
        double2(v.x, v.y);
    }
    default void double2(double[] v) {
        double2(v[0], v[1]);
    }

    default void double3(double x, double y, double z) {
        double1(x);
        double1(y);
        double1(z);
    }
    default void double3(Vector3d v) {
        double3(v.x, v.y, v.z);
    }
    default void double3(double[] v) {
        double3(v[0], v[1], v[2]);
    }

    default void double4(double x, double y, double z, double w) {
        double1(x);
        double1(y);
        double1(z);
        double1(w);
    }
    default void double4(Vector4d v) {
        double4(v.x, v.y, v.z, v.w);
    }
    default void double4(double[] v) {
        double4(v[0], v[1], v[2], v[3]);
    }
}
