package com.vke.core.parsing.config.schema.vks.doc;

import com.vke.api.parsing.config.node.ConfigArrayNode;
import com.vke.api.parsing.config.node.ConfigNode;
import com.vke.api.parsing.config.node.ConfigObjectNode;
import com.vke.core.parsing.config.schema.vks.parser.VksPullParser;
import com.vke.core.parsing.config.schema.vks.parser.type.ObjectTypeDeclaration;
import com.vke.core.parsing.config.schema.vks.parser.type.StringTypeDeclaration;
import com.vke.core.parsing.config.schema.vks.parser.type.VksTypeDeclaration;
import com.vke.core.parsing.config.utils.StringConfigValue;
import com.vke.utils.iter.Iter;

import java.io.IOException;
import java.util.*;

public class VksSchema implements ConfigObjectNode {
    private final ArrayList<String> links;
    private final HashMap<String, VksTypeDeclaration> typedefs;
    private final List<ObjectTypeDeclaration.Field> schemaFields;

    private final Map<String, ConfigNode> map;

    public VksSchema(VksPullParser parser) throws IOException {
        this.links = new ArrayList<>();
        this.typedefs = new HashMap<>();
        this.schemaFields = new ArrayList<>();

        ObjectTypeDeclaration root = new ObjectTypeDeclaration(parser);
        ObjectTypeDeclaration.Field linkField = root.findField("link");
        if (linkField != null) {
            VksTypeDeclaration linkType = linkField.type();
            if (linkType instanceof StringTypeDeclaration linkStrType) {
                this.links.addAll(Arrays.asList(linkStrType.getEnumValues()));
            } else {
                throw new IOException("Field 'link' must be a string with enum values!");
            }
        }

        ObjectTypeDeclaration.Field typedefField = root.findField("typedefs");
        if (typedefField != null) {
            VksTypeDeclaration typedefType = typedefField.type();
            if (typedefType instanceof ObjectTypeDeclaration typedefObjType) {
                List<ObjectTypeDeclaration.Field> fields = typedefObjType.getFields();
                for (ObjectTypeDeclaration.Field field : fields) {
                    typedefs.put(field.name(), field.type());
                }
            } else {
                throw new IOException("Field 'typedefs' must be object!");
            }
        }

        ObjectTypeDeclaration.Field schemaField = root.findField("schema");
        if (schemaField == null) throw new IOException("vks schema needs a 'schema' field!");
        VksTypeDeclaration schemaType = schemaField.type();
        if (schemaType instanceof ObjectTypeDeclaration schemaObjType) {
            this.schemaFields.addAll(schemaObjType.getFields());
        } else {
            throw new IOException("Field 'schema' must be object!");
        }

        this.map = new HashMap<>();
        if (!links.isEmpty()) {
            map.put("link", new LinkArray());
        }
        if (!typedefs.isEmpty()) {
            map.put("typedefs", new TypedefArray());
        }
        map.put("fields", new SchemaFieldArray());
    }

    @Override
    public ConfigNode getNode(String key) {
        return getDescendants().get(key);
    }

    @Override
    public Map<String, ? extends ConfigNode> getDescendants() {
        return map;
    }

    private class LinkArray implements ConfigArrayNode {
        private final StringConfigValue[] arr;

        private LinkArray() {
            this.arr = Iter.of(links).map(StringConfigValue::new).toArray();
        }

        @Override
        public ConfigNode[] values() {
            return arr;
        }
    }

    private class TypedefArray implements ConfigArrayNode {
        private final TypedefObject[] arr;

        private TypedefArray() {
            arr = new TypedefObject[typedefs.size()];
            int i = 0;
            for (Map.Entry<String, VksTypeDeclaration> entry : typedefs.entrySet()) {
                arr[i++] = new TypedefObject(entry.getKey(), entry.getValue());
            }
        }

        @Override
        public ConfigNode[] values() {
            return arr;
        }
    }

    private static class TypedefObject implements ConfigObjectNode {
        private final Map<String, ConfigNode> map;

        private TypedefObject(String name, VksTypeDeclaration type) {
            map = new HashMap<>();
            map.put("name", new StringConfigValue(name));
            map.putAll(type.buildDescendantsMap());
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

    private class SchemaFieldArray implements ConfigArrayNode {
        private final ObjectTypeDeclaration.FieldObject[] arr;

        private SchemaFieldArray() {
            arr = new ObjectTypeDeclaration.FieldObject[schemaFields.size()];
            for (int i = 0; i < arr.length; i++) {
                arr[i] = new ObjectTypeDeclaration.FieldObject(schemaFields.get(i));
            }
        }

        @Override
        public ConfigNode[] values() {
            return arr;
        }
    }
}
