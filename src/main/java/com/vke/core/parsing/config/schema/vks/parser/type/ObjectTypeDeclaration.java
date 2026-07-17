package com.vke.core.parsing.config.schema.vks.parser.type;

import com.vke.api.parsing.config.node.ConfigArrayNode;
import com.vke.api.parsing.config.node.ConfigNode;
import com.vke.api.parsing.config.node.ConfigObjectNode;
import com.vke.core.parsing.config.schema.vks.parser.VksPullParser;
import com.vke.core.parsing.config.schema.vks.parser.tkn.VksTT;
import com.vke.core.parsing.config.utils.BooleanConfigValue;
import com.vke.core.parsing.config.utils.StringConfigValue;
import com.vke.utils.iter.Iter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ObjectTypeDeclaration implements VksTypeDeclaration {
    private List<Field> fields = new ArrayList<>();

    public ObjectTypeDeclaration(VksPullParser parser) throws IOException {
        parser.expect(VksTT.LBrace);
        while (!parser.peek(VksTT.RBrace)) {
            boolean isRequired = false;
            if (parser.peek(VksTT.Exclamation)) {
                parser.next();
                isRequired = true;
            }
            String fieldName = parser.parseThing(VksPullParser::parseString);
            parser.expect(VksTT.Colon);
            VksTypeDeclaration type = parser.parseThing(VksPullParser::parseTypeDeclaration);
            parser.expect(VksTT.Semicolon);
            fields.add(new Field(isRequired, fieldName, type));
        }
        parser.expect(VksTT.RBrace);
    }

    public Field findField(String name) {
        return Iter.of(fields).filter(f -> f.name.equals(name)).next().unwrapOrNull();
    }

    public List<Field> getFields() {
        return fields;
    }

    @Override
    public Map<String, ? extends ConfigNode> buildDescendantsMap() {
        if (fields.isEmpty()) {
            return Map.of("type", new StringConfigValue("object"));
        } else {
            return Map.of("type", new StringConfigValue("object"), "fields", new FieldsArray());
        }
    }

    public record Field(boolean isRequired, String name, VksTypeDeclaration type) {
    }

    private class FieldsArray implements ConfigArrayNode {
        private final FieldObject[] arr;

        private FieldsArray() {
            arr = new FieldObject[fields.size()];
            for (int i = 0; i < arr.length; i++) {
                arr[i] = new FieldObject(fields.get(i));
            }
        }

        @Override
        public ConfigNode[] values() {
            return arr;
        }
    }

    public static class FieldObject implements ConfigObjectNode {
        private final Map<String, ConfigNode> map;

        public FieldObject(Field field) {
            map = new HashMap<>();
            map.put("required", new BooleanConfigValue(field.isRequired()));
            map.put("name", new StringConfigValue(field.name()));
            map.putAll(field.type.buildDescendantsMap());
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
