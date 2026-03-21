package com.vke.api.rendering.vulkan.descriptors.types;

import java.util.HashMap;

public class StructType extends TypeLayout {

    public HashMap<String, Member> members;

    public static class Member {
        public String name;
        public long offset;
        public long size;
        public TypeLayout type;
    }

}
