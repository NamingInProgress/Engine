package com.vke.core.parsing.config.schema.elements.types;

import com.vke.api.parsing.config.Configs;
import com.vke.api.parsing.config.node.ConfigArrayNode;
import com.vke.api.parsing.config.node.ConfigNode;
import com.vke.core.file.deflate.BitUtils;
import com.vke.core.parsing.config.schema.JsonMarker;
import com.vke.core.parsing.config.schema.elements.SchemaHeader;
import com.vke.core.parsing.config.schema.elements.SchemaElement;

import java.util.List;

public abstract class SchemaType extends SchemaElement {
    private static final int UF_LOCAL = 1;

    @JsonMarker("type")
    protected Type type;
    protected int usageFlags;

    public SchemaType(ConfigNode node, SchemaHeader header) {
        super(node, header);
    }

    public static SchemaType getType(ConfigNode node, SchemaHeader ctx) {
        int usageFlags = 0;
        if (node.hasField("usage_flags")) {
            ConfigArrayNode arr = Configs.getArray(node, "usage_flags");
            List<String> usageFlagsList = Configs.getStringList(arr);
            for (String usageFlag : usageFlagsList) {
                if ("local".equals(usageFlag)) usageFlags |= UF_LOCAL;
            }
        }

        String typeName = Configs.getString(node, "type");
        Type t = Type.tryFromString(typeName);
        if (t == null) {
            return ctx.getTypeDef(typeName);
        }
        SchemaType ty = switch (t) {
            case String -> new SchemaStringType(node, ctx);
            case Number -> new SchemaNumberType(node, ctx);
            case Boolean -> new SchemaBoolType(node, ctx);
            case Array -> new SchemaArrayType(node, ctx);
            case Object -> new SchemaObjectType(node, ctx);
            case Meta -> new SchemaMetaType(node, ctx);
            case Multi -> new SchemaMultiType(node, ctx);
        };
        ty.usageFlags = usageFlags;
        return ty;
    }

    public int getUsageFlags() {
        return usageFlags;
    }

    public boolean isLocal() {
        return BitUtils.bitsContains(usageFlags, UF_LOCAL);
    }

    public enum Type {
        String("string"),
        Number("number"),
        Boolean("boolean"),
        Array("array"),
        Object("object"),
        Meta("meta"),
        Multi("multi");

        public final String repr;

        Type(String repr) {
            this.repr = repr;
        }

        public static Type tryFromString(String typeName) {
            for (Type t : values()) {
                if (t.repr.equals(typeName)) {
                    return t;
                }
            }
            return null;
        }
    }
}
