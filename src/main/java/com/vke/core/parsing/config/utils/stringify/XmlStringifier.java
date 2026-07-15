package com.vke.core.parsing.config.utils.stringify;

import com.vke.api.parsing.config.node.*;

import java.util.Map;

public class XmlStringifier {
    public static String stringify(ConfigNode node, String root) {
        StringBuilder b = new StringBuilder();
        node(root, b, node);
        return b.toString();
    }

    private static void node(String lastField, StringBuilder builder, ConfigNode node) {
        if (node instanceof ConfigArrayNode arr) {
            builder.append('<');
            builder.append(lastField);
            builder.append('>');
            int i = 0;
            ConfigNode[] vals = arr.values();
            for (ConfigNode item : vals) {
                node(lastField, builder, item);
                if (++i < vals.length) {
                    builder.append(',');
                }
            }
            builder.append("</");
            builder.append(lastField);
            builder.append(">");
        } else if (node instanceof ConfigObjectNode obj) {
            builder.append('<');
            builder.append(lastField);
            builder.append('>');
            Map<String , ? extends ConfigNode> map = obj.getDescendants();
            for (Map.Entry<String, ? extends ConfigNode> field : map.entrySet()) {
                node(field.getKey(), builder, field.getValue());
            }
            builder.append("</");
            builder.append(lastField);
            builder.append(">");
        } else if (node instanceof ConfigValueNode value) {
            builder.append('<');
            builder.append(lastField);
            builder.append('>');
            builder.append(value.getValue());
            builder.append("</");
            builder.append(lastField);
            builder.append(">");
        } else if (node instanceof ConfigNumberNode number) {
            builder.append("<");
            builder.append(lastField);
            builder.append('>');
            builder.append(number.getValue());
            builder.append("</");
            builder.append(lastField);
            builder.append(">");
        } else if (node instanceof ConfigBooleanNode bool) {
            builder.append('<');
            builder.append(lastField);
            builder.append('>');
            builder.append(bool.getValue());
            builder.append("</");
            builder.append(lastField);
            builder.append(">");
        }
    }
}
