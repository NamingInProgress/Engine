package com.vke.api.pipeline.fucvk;

import com.carrotsearch.hppc.IntObjectHashMap;
import com.carrotsearch.hppc.ObjectIntHashMap;
import com.carrotsearch.hppc.cursors.IntObjectCursor;
import com.vke.api.parsing.config.ConfigDocument;
import com.vke.api.parsing.config.ConfigParser;
import com.vke.api.parsing.config.schema.ConfigSchema;
import com.vke.api.parsing.config.schema.SchemaMismatchException;
import com.vke.api.pipeline.Entry;
import com.vke.api.pipeline.Struct;
import com.vke.core.parsing.config.json.JsonParser;
import com.vke.core.parsing.config.xml.XmlParser;
import com.vke.core.vulkan.descriptor.DescriptorType;
import com.vke.core.vulkan.descriptor.wrapper.JsonDescriptorData;
import com.vke.core.vulkan.shader.Shader;
import com.vke.utils.io.Identifier;
import com.vke.utils.tuple.Pair;
import com.vke.utils.Utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public abstract class DescriptorData {
    private static ConfigSchema schema;

    static {
        try {
            Identifier ident = new Identifier("schema/layouts.schema.json");
            char[] source = Utils.readCharsFromInputStream(ident.asInputStream());
            ConfigParser parser = new JsonParser();
            parser.setSource(source);
            ConfigDocument schemaDoc = parser.parse();
            schema = ConfigSchema.readVke(schemaDoc, ident.getPath());
        } catch (IOException | ConfigParser.ConfigParseException e) {
            throw new RuntimeException(e);
        }

    }

    protected final IntObjectHashMap<Set> sets = new IntObjectHashMap<>();
    protected final HashMap<String, Pair<Integer, Integer>> entryPositions = new HashMap<>();
    protected final ObjectIntHashMap<DescriptorType> counts = new ObjectIntHashMap<>();

    public int getSetsAmount() { return sets.size(); }

    public Set getSet(int index) {
        return sets.get(index);
    }

    public ObjectIntHashMap<DescriptorType> counts() {
        if (counts.isEmpty()) {
            for (IntObjectCursor<Set> set : sets) {
                for (IntObjectCursor<Binding> binding : set.value.bindings) {
                    counts.addTo(DescriptorType.fromWrapper(binding.value.type), 1);
                }
            }
        }

        return counts;
    }

    public IntObjectHashMap<Set> getSets() { return this.sets; }

    public Entry getEntry(String entryName) {
        return fromPositionAndName(getPosition(entryName), entryName);
    }

    public Entry fromPositionAndName(Pair<Integer, Integer> pos, String name) {
        return getSet(pos.v1).getBinding(pos.v2).getEntry(name);
    }

    public Pair<Integer, Integer> getPosition(String name) {
        Pair<Integer, Integer> location = entryPositions.get(name);

        if (location != null) return location;

        for (IntObjectCursor<Set> set : sets) {
            Pair<Integer, Integer> possiblePos = set.value.getEntryPosition(set.key, name);
            if (possiblePos != null) {
                entryPositions.put(name, possiblePos);
                return possiblePos;
            }
        }

        throw new IllegalStateException("Failed to find entry for name: " + name);
    }

    public static DescriptorData fromFileWithExtension(String extension, Identifier ident) throws IOException, ConfigParser.ConfigParseException, SchemaMismatchException {
        ConfigParser parser = null;

        if (extension.equalsIgnoreCase("json")) {
            parser = new JsonParser();
        } else if (extension.equalsIgnoreCase("xml")) {
            parser = new XmlParser();
        }

        parser.setSource(Utils.readCharsFromInputStream(ident.asInputStream()));

        if (extension.equalsIgnoreCase("json")) {
            ConfigDocument doc = parser.parse();
            doc.validate(schema, ident.getPath());
            return JsonDescriptorData.fromJson(doc);
        }

        if (extension.equalsIgnoreCase("xml")) {
            ConfigDocument doc = parser.parse(ConfigParser.PARSE_LITERALS | ConfigParser.ATTRIBS_TO_FIELDS);
            doc.validate(schema, ident.getPath());
            return JsonDescriptorData.fromJson(doc);
        }

        return null;
    }

    public static abstract class Set {

        protected final IntObjectHashMap<Binding> bindings = new IntObjectHashMap<>();

        public Binding getBinding(int index) {
            return bindings.get(index);
        }

        public Pair<Integer, Integer> getEntryPosition(int thisIndex, String entryName) {
            for (IntObjectCursor<Binding> binding : bindings) {
                if (binding.value.getEntry(entryName) != null) {
                    return new Pair<>(thisIndex, binding.key);
                }
            }
            return null;
        }

        public IntObjectHashMap<Binding> getBindings() { return this.bindings; }

    }

    public static abstract class Binding {

        protected Type type;
        protected Shader.Stages stages;
        protected String name;
        protected Struct struct;

        public Entry getEntry(String name) {
            return struct.byName(name);
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

        public Type getType() { return this.type; }
        public String getName() { return name; }
        public Struct getStruct() { return struct; }
        public Shader.Stages getStages() { return this.stages; }
    }

    public static abstract class Struct {

        protected final ArrayList<Entry> entries = new ArrayList<>();
        protected final ObjectLongHashMap<Entry> precedings = new ObjectLongHashMap<>();

        public ArrayList<Entry> getEntries() {
            return this.entries;
        }

        public Entry byName(String name) {
            return entries.stream().filter(c -> c.name.equals(name)).findFirst().orElse(null);
        }

        public long preceding(String name) {
            return preceding(byName(name));
        }
        
        public long preceding(Entry e) {
            if (precedings.containsKey(e)) return precedings.get(e);

            int idx = entries.indexOf(e);
            
            long count = 0;
            for (int i = 0; i < idx; i++) {
                count += entries.get(i).getSize();
            }

            precedings.put(e, count);
            
            return count;
        }

        public int sizeof() { return entries.stream().mapToInt(Entry::getSize).sum(); }

    }

    public static abstract class Entry {

        protected String name;
        protected Type type;
        protected boolean auto;

        public int getSize() { return type.bytes(); }

        public enum Type {

            MAT4("mat4", 64),
            FLOAT("float", 4),
            FLOAT2("float2", 8),
            FLOAT3("float3", 12),
            FLOAT4("float4", 16),
            SAMPLER2D("sampler2D", 0),
            IMAGE2D("image2D", 0);

            private final String name;
            private final int bytes;

            Type(String name, int bytes) {
                this.name = name;
                this.bytes = bytes;
            }

            public String getName() { return this.name; }

            public int bytes() { return bytes; }

            public static Type fromString(String name) {
                return Arrays.stream(Type.values()).filter(c -> c.getName().equals(name)).findFirst().orElse(null);
            }

        }

    }

}
