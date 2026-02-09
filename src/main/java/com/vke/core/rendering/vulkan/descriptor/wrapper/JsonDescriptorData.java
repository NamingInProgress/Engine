package com.vke.core.rendering.vulkan.descriptor.wrapper;

import com.carrotsearch.hppc.IntObjectHashMap;
import com.carrotsearch.hppc.ObjectArrayList;
import com.vke.api.parsing.config.ConfigDocument;
import com.vke.api.parsing.config.node.ConfigArrayNode;
import com.vke.api.parsing.config.node.ConfigNumberNode;
import com.vke.api.parsing.config.node.ConfigObjectNode;
import com.vke.api.parsing.config.node.ConfigValueNode;
import com.vke.utils.Utils;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.Arrays;

public class JsonDescriptorData {

    private final IntObjectHashMap<Set> sets = new IntObjectHashMap<>();

    public static JsonDescriptorData fromJson(ConfigDocument root) {
        JsonDescriptorData self = new JsonDescriptorData();

        ConfigArrayNode sets = root.getArray("sets");
        for (ConfigObjectNode set : (ConfigObjectNode[]) sets.values()) {
            self.sets.put((int) ((ConfigNumberNode) set.getNode("set")).getValue(), Set.fromJson((ConfigArrayNode) set.getNode("layouts")));
        }

        return self;
    }


    public static class Set {

        private final IntObjectHashMap<Binding> bindings = new IntObjectHashMap<>();

        public static Set fromJson(ConfigArrayNode layouts) {
            Set self = new Set();

            int i = 0;
            for (ConfigObjectNode binding : (ConfigObjectNode[]) layouts.values()) {
                self.bindings.put(i, Binding.fromJson(binding));
                i++;
            }

            return self;
        }

    }

    public static class Binding {

        Type type;
        String name;
        Struct struct;

        @SuppressWarnings("all")
        public static Binding fromJson(ConfigObjectNode binding) {
            Binding self = new Binding();

            String t = ((ConfigValueNode) binding.getNode("type")).getValue();
            self.type = Type.fromString(t);

            self.name = ((ConfigValueNode) binding.getNode("name")).getValue();

            self.struct = Struct.fromJson((ConfigArrayNode) binding.getNode("struct"));

            if (self.type == null) {
                throw new IllegalStateException("Failed to get binding type for type: " + t);
            }

            return self;
        }

        public enum Type {

            COMBINED_IMAGE_SAMPLER("combined_image_sampler", "cis"),
            STORAGE_IMAGE("storage_image", "si"),
            UNIFORM_BUFFER("uniform_buffer", "UBO"),
            STORAGE_BUFFER("storage_buffer", "SSBO");

            private final String[] names;

            Type(String... names) {
                this.names = names;
            }

            public String[] getNames() { return this.names; }

            public static Type fromString(String name) {
                return Arrays.stream(Type.values()).filter(c -> Utils.arrayContains(c.getNames(), name)).findFirst().orElse(null);
            }

        }

    }

    public static class Struct {

        private final ArrayList<Entry> entries = new ArrayList<>();

        public static Struct fromJson(ConfigArrayNode struct) {
            Struct self = new Struct();

            for (ConfigObjectNode entry : (ConfigObjectNode[]) struct.values()) {
                ConfigObjectNode pad = (ConfigObjectNode) entry.getNode("padded");

                self.entries.add(Entry.fromRegular(entry));

                if (pad != null) {
                    self.entries.add(Entry.fromPadding(pad));
                }
            }

            return self;
        }

    }

    public static class Entry {

        String name;
        Type type;
        boolean auto;

        public static Entry fromRegular(ConfigObjectNode json) {
            Entry self = new Entry();

            self.auto = json.getNode("auto");
        }

        public static Entry fromPadding(ConfigObjectNode json) {

        }

        public enum Type {

            MAT4("mat4"),
            FLOAT("float"),
            FLOAT2("float2"),
            FLOAT3("float3"),
            FLOAT4("float4");

            private final String name;

            Type(String name) {
                this.name = name;
            }

            public String getName() { return this.name; }

            public Type fromString(String name) {
                return Arrays.stream(Type.values()).filter(c -> c.getName().equals(name)).findFirst().orElse(null);
            }

        }

    }

}
