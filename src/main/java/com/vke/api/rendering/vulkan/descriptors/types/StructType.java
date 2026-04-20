package com.vke.api.rendering.vulkan.descriptors.types;

import java.util.HashMap;

public class StructType extends TypeLayout {

    public HashMap<String, Member> members = new HashMap<>();

    public static class Member {
        public String name;
        public long offset;
        public long size;
        public TypeLayout type;

        public Member(String name, long offset, long size, TypeLayout type) {
            this.name = name;
            this.offset = offset;
            this.size = size;
            this.type = type;
        }
    }

}
