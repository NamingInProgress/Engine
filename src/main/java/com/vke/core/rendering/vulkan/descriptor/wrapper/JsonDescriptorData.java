package com.vke.core.rendering.vulkan.descriptor.wrapper;

import com.vke.api.parsing.config.ConfigDocument;
import com.vke.api.parsing.config.Configs;
import com.vke.api.parsing.config.node.*;
import com.vke.api.vulkan.descriptors.DescriptorData;

public class JsonDescriptorData extends DescriptorData {

    private JsonDescriptorData() {}

    public static JsonDescriptorData fromJson(ConfigDocument root) {
        JsonDescriptorData self = new JsonDescriptorData();

        ConfigArrayNode sets = root.getArray("sets");
        for (ConfigNode s : sets.values()) {
            ConfigObjectNode set = (ConfigObjectNode) s;
            self.sets.put((int) ((ConfigNumberNode) set.getNode("set")).getValue(), JsonSet.fromJson((ConfigArrayNode) set.getNode("layouts")));
        }

        return self;
    }


    public static class JsonSet extends DescriptorData.Set {

        public static JsonSet fromJson(ConfigArrayNode layouts) {
            JsonSet self = new JsonSet();

            int i = 0;
            for (ConfigNode b :  layouts.values()) {
                ConfigObjectNode binding = (ConfigObjectNode) b;
                self.bindings.put(i, JsonBinding.fromJson(binding));
                i++;
            }

            return self;
        }

    }

    public static class JsonBinding extends DescriptorData.Binding {

        @SuppressWarnings("all")
        public static JsonBinding fromJson(ConfigObjectNode binding) {
            JsonBinding self = new JsonBinding();

            String t = ((ConfigValueNode) binding.getNode("type")).getValue();
            self.type = Type.fromString(t);

            self.name = ((ConfigValueNode) binding.getNode("name")).getValue();

            self.struct = JsonStruct.fromJson((ConfigArrayNode) binding.getNode("struct"));

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
                ConfigObjectNode pad = (ConfigObjectNode) entry.getNode("padded");

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

            ConfigBooleanNode autoObject = ((ConfigBooleanNode) json.getNode("auto"));

            self.auto = autoObject != null && autoObject.getValue();
            self.name = ((ConfigValueNode) json.getNode("name")).getValue();
            self.type = Type.fromString(((ConfigValueNode) json.getNode("type")).getValue());

            return self;
        }

        public static JsonEntry fromPadding(ConfigObjectNode json) {
            JsonEntry self = fromRegular(json);
            self.auto = true;
            return self;
        }

    }

}
