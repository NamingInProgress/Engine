package com.vke.api.rendering.vulkan.descriptors.types;

import java.util.List;

public class StructType extends TypeLayout {

    public List<Member> members;

    public static class Member {
        public String name;
        public long offset;
        public TypeLayout type;
    }

}
