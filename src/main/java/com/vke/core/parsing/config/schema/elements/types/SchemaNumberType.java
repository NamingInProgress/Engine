package com.vke.core.parsing.config.schema.elements.types;

import com.vke.api.parsing.config.Configs;
import com.vke.api.parsing.config.node.ConfigNode;
import com.vke.api.parsing.config.node.ConfigNumberNode;
import com.vke.api.parsing.config.node.ConfigObjectNode;
import com.vke.api.parsing.config.schema.SchemaValidationResult;
import com.vke.core.parsing.config.schema.JsonMarker;
import com.vke.core.parsing.config.schema.elements.SchemaHeader;

import java.util.ArrayDeque;

public class SchemaNumberType extends SchemaType {
    @JsonMarker("range")
    private Range range;

    public SchemaNumberType(ConfigNode node, SchemaHeader ctx) {
        super(node, ctx);
        this.type = Type.Number;

        ConfigObjectNode rangeObj = Configs.getObject(node, "range");
        if (rangeObj != null) {
            Range r = new Range();
            r.min = Configs.getNumberSafe(rangeObj, "min");
            r.max = Configs.getNumberSafe(rangeObj, "max");
            this.range = r;
        }
    }

    @Override
    public void validate(ConfigNode node, SchemaValidationResult result, ArrayDeque<String> path) {
        if (node instanceof ConfigNumberNode numberNode) {
            if (range != null) {
                float val = numberNode.getValue();
                range.validate(val, result, path);
            }
        } else {
            result.addError(SchemaValidationResult.ValidationError.illegalType(node, ConfigNode.Type.Boolean, path));
        }
    }

    public Range getRange() {
        return range;
    }

    public static class Range {
        @JsonMarker("min")
        private Float min;
        @JsonMarker("max")
        private Float max;

        private void validate(float val, SchemaValidationResult result, ArrayDeque<String> path) {
            if (min != null && min > val) {
                String msg = String.format("%f is smaller than minimum value %f!", val, min);
                result.addError(new SchemaValidationResult.ValidationError(msg, path));
            }
            if (max != null && max > val) {
                String msg = String.format("%f is bigger than minimum value %f!", val, max);
                result.addError(new SchemaValidationResult.ValidationError(msg, path));
            }
        }
    }
}
