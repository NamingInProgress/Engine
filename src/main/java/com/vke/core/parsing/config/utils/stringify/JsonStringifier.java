package com.vke.core.parsing.config.utils.stringify;

import com.vke.api.parsing.config.node.*;

import java.util.Map;

public class JsonStringifier {
    public static String stringify(ConfigNode node) {
        StringBuilder b = new StringBuilder();
        node(b, node);
        return b.toString();
    }

    private static void node(StringBuilder builder, ConfigNode node) {
        if (node instanceof ConfigArrayNode arr) {
            builder.append('[');
            int i = 0;
            ConfigNode[] vals = arr.values();
            for (ConfigNode item : vals) {
                node(builder, item);
                if (++i < vals.length) {
                    builder.append(',');
                }
            }
            builder.append(']');
        } else if (node instanceof ConfigObjectNode obj) {
            builder.append('{');
            int i = 0;
            Map<String , ? extends ConfigNode> map = obj.getDescendants();
            for (Map.Entry<String, ? extends ConfigNode> field : map.entrySet()) {
                builder.append('"');
                builder.append(field.getKey());
                builder.append('"');
                builder.append(':');
                node(builder, field.getValue());
                if (++i < map.size()) {
                    builder.append(',');
                }
            }
            builder.append('}');
        } else if (node instanceof ConfigValueNode value) {
            builder.append('"');
            builder.append(value.getValue());
            builder.append('"');
        } else if (node instanceof ConfigNumberNode number) {
            builder.append(number.getNumber());
        } else if (node instanceof ConfigBooleanNode bool) {
            builder.append(bool.getBoolean());
        }
    }
}
