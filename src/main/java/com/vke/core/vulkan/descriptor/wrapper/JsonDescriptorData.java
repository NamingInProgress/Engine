package com.vke.core.vulkan.descriptor.wrapper;

import com.vke.api.parsing.config.ConfigDocument;
import com.vke.api.parsing.config.node.*;
import com.vke.api.vulkan.descriptors.DescriptorData;
import com.vke.core.vulkan.shader.Shader;

public class JsonDescriptorData extends DescriptorData {

    private JsonDescriptorData() {}

    public static JsonDescriptorData fromJson(ConfigDocument root) {
        JsonDescriptorData self = new JsonDescriptorData();

        ConfigArrayNode sets = root.getArray("sets");
        for (ConfigNode set : sets.values()) {
            self.sets.put(set.getInt("set"), JsonSet.fromJson(set.getArray("layouts")));
        }

        return self;
    }


    public static class JsonSet extends DescriptorData.Set {

        public static JsonSet fromJson(ConfigArrayNode layouts) {
            JsonSet self = new JsonSet();

            int i = 0;
            for (ConfigNode binding :  layouts.values()) {
                self.bindings.put(i, JsonBinding.fromJson((ConfigObjectNode) binding));
                i++;
            }

            return self;
        }

    }

    public static class JsonBinding extends DescriptorData.Binding {

        @SuppressWarnings("all")
        public static JsonBinding fromJson(ConfigObjectNode binding) {
            JsonBinding self = new JsonBinding();

            String t = binding.getString("type");
            self.type = Type.fromString(t);

            self.name = binding.getString("name");

            self.struct = JsonStruct.fromJson(binding.getArray("struct"));

            ConfigArrayNode stages = binding.getArray("stages");

            String[] names = new String[stages.values().length];

            ConfigNode[] values = stages.values();
            for (int i = 0; i < values.length; i++) {
                ConfigNode value = values[i];
                names[i] = ((ConfigValueNode) value).getValue();
            }

            self.stages = Shader.Stages.fromString(names);

            if (self.type == null) {
                throw new IllegalStateException("Failed to get binding type for type: " + t);
            }

            return self;
        }

    }

    public static class JsonStruct extends DescriptorData.Struct {

        public static JsonStruct fromJson(ConfigArrayNode struct) {
            JsonStruct self = new JsonStruct();

            for (ConfigNode e : struct.values()) {
                ConfigObjectNode entry = (ConfigObjectNode) e;
                ConfigObjectNode pad = entry.getObject("padded");

                self.entries.add(JsonEntry.fromRegular(entry));

                if (pad != null) {
                    self.entries.add(JsonEntry.fromPadding(pad));
                }
            }

            return self;
        }

    }

    public static class JsonEntry extends DescriptorData.Entry {

        public static JsonEntry fromRegular(ConfigObjectNode json) {
            JsonEntry self = new JsonEntry();

            Boolean auto = json.getBooleanSafe("auto");

            self.auto = auto != null && auto;
            self.name = json.getString("name");
            self.type = Type.fromString(json.getString("type"));

            return self;
        }

        public static JsonEntry fromPadding(ConfigObjectNode json) {
            JsonEntry self = fromRegular(json);
            self.auto = true;
            return self;
        }

    }

}
