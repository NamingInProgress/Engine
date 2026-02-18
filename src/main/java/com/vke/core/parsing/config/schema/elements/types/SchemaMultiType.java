package com.vke.core.parsing.config.schema.elements.types;

import com.vke.api.parsing.config.Configs;
import com.vke.api.parsing.config.node.ConfigArrayNode;
import com.vke.api.parsing.config.node.ConfigNode;
import com.vke.api.parsing.config.schema.SchemaElementLocation;
import com.vke.api.parsing.config.schema.SchemaValidationResult;
import com.vke.core.parsing.config.schema.elements.SchemaHeader;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SchemaMultiType extends SchemaType {
    private final List<SchemaType> allowed;

    public SchemaMultiType(ConfigNode node, SchemaHeader ctx) {
        super(node, ctx);
        this.type = Type.Multi;
        ConfigArrayNode arr = Configs.getArray(node, "allowed");
        this.allowed = new ArrayList<>();
        for (ConfigNode typeNode : arr.values()) {
            allowed.add(SchemaType.getType(typeNode, ctx));
        }
    }

    @Override
    public void validate(ConfigNode node, SchemaValidationResult result, SchemaElementLocation path) {
        for (SchemaType allowedType : allowed) {
            SchemaValidationResult tmpResult = new SchemaValidationResult();
            allowedType.validate(node, tmpResult, path);
            if (tmpResult.isValid()) {
                //valid type found
                return;
            }
        }
        //no type is valid so were going to populate result accordingly
        String allowedStr = allowed.stream().map(a -> a.type.toString()).collect(Collectors.joining(","));
        String message = String.format("Illegal type found\"%s\"! Allowed field types are: [%s]", node.getType(), allowedStr);
        result.addError(new SchemaValidationResult.ValidationError(message, path));
    }
}
