package com.vke.core.rendering.reflection2;

import com.carrotsearch.hppc.IntObjectHashMap;
import com.vke.api.rendering.vulkan.pipeline.BaseType;
import com.vke.core.rendering.reflection2.api.DescriptorCategory;

public class SpirvItem {
    public String name = "";
    public BaseType type = BaseType.Unknown;

    public boolean rowMajor;
    public int arrayStride;
    public int matrixStride;
    public int byteOffset;
    public int location = -1;
    public int binding = -1;
    public int set = -1;
    public DescriptorCategory category;
    public boolean rootOrIsVec;
    public int bitWidth;

    // also value for constants (dw about parser internal cuz spirv retarded)
    public long scalarBits;
    public boolean block;

    public IntObjectHashMap<Member> members = new IntObjectHashMap<>();
    public SpirvItem componentType;

    @Override
    public String toString() {
        return "SpirvType{" +
                "name='" + name + '\'' +
                ", type=" + type +
                ", rowMajor=" + rowMajor +
                ", arrayStride=" + arrayStride +
                ", matrixStride=" + matrixStride +
                ", byteOffset=" + byteOffset +
                ", location=" + location +
                ", binding=" + binding +
                ", set=" + set +
                ", length=" + scalarBits +
                ", block=" + block +
                ", members=" + members +
                ", componentType=" + componentType +
                "}\n";
    }

    public static class Member {
        public String name = "";
        public int offset;
        public SpirvItem type;

        @Override
        public String toString() {
            return "Member{" +
                    "name='" + name + '\'' +
                    ", offset=" + offset +
                    ", type=" + type +
                    '}';
        }
    }

}
