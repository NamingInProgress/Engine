package com.vke.core.parsing.config.schema.vks.parser.type;

import com.vke.api.parsing.config.node.ConfigNode;
import com.vke.api.parsing.config.node.ConfigObjectNode;
import com.vke.core.parsing.config.schema.vks.parser.VksPullParser;
import com.vke.core.parsing.config.schema.vks.parser.tkn.VksTT;
import com.vke.core.parsing.config.utils.NumberConfigValue;
import com.vke.core.parsing.config.utils.StringConfigValue;

import java.io.IOException;
import java.util.Map;

public class NumberTypeDeclaration implements VksTypeDeclaration{
    private float min, max;
    private boolean hasMin, hasMax;

    public NumberTypeDeclaration(VksPullParser parser) throws IOException {
        if (parser.peek(VksTT.LBrack)) {
            if (parser.peek(VksTT.DotDot)) {
                parser.next();
                hasMin = false;
            } else {
                min = parser.parseNextArg(VksPullParser::parseNumber);
                hasMin = true;
            }

            if (parser.peek(VksTT.DotDot)) {
                parser.next();
                hasMax = false;
            } else {
                max = parser.parseNextArg(VksPullParser::parseNumber);
                hasMax = true;
            }
        }
    }

    @Override
    public Map<String, ? extends ConfigNode> buildDescendantsMap() {
        if (hasMin || hasMax) {
            return Map.of("type", new StringConfigValue("number"), "range", new RangeObject());
        } else {
            return Map.of("type", new StringConfigValue("number"));
        }
    }

    private class RangeObject implements ConfigObjectNode {
        private final Map<String, ? extends ConfigNode> map;

        private RangeObject() {
            if (hasMin && hasMax) {
                map = Map.of("min", new NumberConfigValue(min), "max", new NumberConfigValue(max));
            } else if (hasMax) {
                map = Map.of("max", new NumberConfigValue(max));
            } else if (hasMin) {
                map = Map.of("min", new NumberConfigValue(min));
            } else {
                map = Map.of();
            }
        }

        @Override
        public ConfigNode getNode(String key) {
            return getDescendants().get(key);
        }

        @Override
        public Map<String, ? extends ConfigNode> getDescendants() {
            return map;
        }
    }
}
