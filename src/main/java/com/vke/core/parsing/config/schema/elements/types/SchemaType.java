package com.vke.core.parsing.config.schema.elements.types;

import com.vke.api.parsing.config.Configs;
import com.vke.api.parsing.config.node.ConfigNode;
import com.vke.core.parsing.config.schema.JsonMarker;
import com.vke.core.parsing.config.schema.elements.SchemaCtx;
import com.vke.core.parsing.config.schema.elements.SchemaElement;

public abstract class SchemaType extends SchemaElement {
    @JsonMarker("type")
    protected Type type;

    public SchemaType(ConfigNode node, SchemaCtx ctx) {
        super(node, ctx);
    }

    public static SchemaType getType(ConfigNode node, SchemaCtx ctx) {
        String typeName = Configs.getString(node, "type");
        Type t = Type.tryFromString(typeName);
        if (t == null) {
            return ctx.getTypeDef(typeName);
        }
        return switch (t) {
            case String -> new SchemaStringType(node, ctx);
            case Number -> new SchemaNumberType(node, ctx);
            case Boolean -> new SchemaBoolType(node, ctx);
            case Array -> new SchemaArrayType(node, ctx);
            case Object -> new SchemaObjectType(node, ctx);
            case Meta -> new SchemaMetaType(node, ctx);
            case Multi -> new SchemaMultiType(node, ctx);
        };
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
