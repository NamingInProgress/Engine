package com.vke.api.rendering.vulkan.descriptors.types;

import java.util.HashMap;
import java.util.Objects;

public class StructType extends TypeLayout {

    public HashMap<String, Member> members = new HashMap<>();

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        StructType that = (StructType) o;
        return Objects.equals(members, that.members);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(members);
    }

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

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) return false;
            Member member = (Member) o;
            return offset == member.offset && size == member.size && Objects.equals(name, member.name) && Objects.equals(type, member.type);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, offset, size, type);
        }
    }

}
