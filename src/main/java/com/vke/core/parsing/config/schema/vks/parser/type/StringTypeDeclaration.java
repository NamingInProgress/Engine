package com.vke.core.parsing.config.schema.vks.parser.type;

import com.vke.api.parsing.config.node.ConfigArrayNode;
import com.vke.api.parsing.config.node.ConfigNode;
import com.vke.core.parsing.config.schema.vks.parser.VksPullParser;
import com.vke.core.parsing.config.schema.vks.parser.tkn.VksTT;
import com.vke.core.parsing.config.utils.StringConfigValue;
import com.vke.utils.iter.Iter;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class StringTypeDeclaration implements VksTypeDeclaration {
    private @Nullable String[] enumValues;

    public StringTypeDeclaration(VksPullParser parser) throws IOException {
        if (parser.peek(VksTT.LBrack)) {
            List<String> args = parser.parseAllArgs(VksPullParser::parseQuotString);
            this.enumValues = args.toArray(new String[0]);
        }
    }

    public String[] getEnumValues() {
        return enumValues;
    }

    @Override
    public Map<String, ? extends ConfigNode> buildDescendantsMap() {
        if (enumValues != null) {
            return Map.of("type", new StringConfigValue("string"), "enum", new EnumArray(enumValues));
        } else {
            return Map.of("type", new StringConfigValue("string"));
        }
    }

    private static class EnumArray implements ConfigArrayNode {
        private final StringConfigValue[] values;

        private EnumArray(String[] arr) {
            this.values = Iter.of(arr).map(StringConfigValue::new).toArray();
        }

        @Override
        public ConfigNode[] values() {
            return values;
        }
    }
}
