package com.vke.api.pipeline;

import com.vke.api.abstraction.descriptors.buffer.PackingType;

import java.util.Arrays;

public abstract class Entry {

    protected String name;
    protected Type type;
    protected boolean auto;

    protected final PackingType packingType;

    public Entry(PackingType packingType) {
        this.packingType = packingType;
    }

    public int getSize() {
        return packingType == PackingType.STD140 ? type.std140bytes() : type.std430bytes();
    }

    public enum Type {

        FLOAT("float",   4,  4),
        FLOAT2("float2", 8,  8),
        FLOAT3("float3", 16, 12),
        FLOAT4("float4", 16, 16),
        MAT2("mat2",     32, 16),
        MAT3("mat3",     48, 36),
        MAT4("mat4",     64, 64),

        SAMPLER2D("sampler2D", 0, 0),
        IMAGE2D("image2D", 0, 0);

        private final String name;
        private final int std140bytes, std430bytes;

        Type(String name, int std140bytes, int std430bytes) {
            this.name = name;
            this.std140bytes = std140bytes;
            this.std430bytes = std430bytes;
        }

        public String getName() {
            return this.name;
        }

        public int std140bytes() {
            return std140bytes;
        }
        public int std430bytes() { return std430bytes; }

        public static Type fromString(String name) {
            return Arrays.stream(Type.values()).filter(c -> c.getName().equals(name)).findFirst().orElse(null);
        }

    }

}
