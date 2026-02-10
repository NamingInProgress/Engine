package com.vke.api.parsing.config.schema;

import com.vke.api.parsing.config.node.ConfigNode;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SchemaValidationResult {
    private boolean valid;
    private List<ValidationError> errors;

    public SchemaValidationResult() {
        this.valid = true;
        this.errors = new ArrayList<>();
    }

    public void addError(ValidationError error) {
        this.valid = false;
        this.errors.add(error);
    }

    public boolean isValid() {
        return valid;
    }

    public List<ValidationError> getErrors() {
        return errors;
    }

    public static class ValidationError {
        private final String message;

        public ValidationError(String message, SchemaElementLocation path) {
            this.message = message + '\n' + path.getFormatted();
        }

        public static ValidationError missingField(SchemaElementLocation path, String field) {
            String msg = String.format("Missing required field \"%s\"!", field);
            return new ValidationError(msg, path);
        }

        public static ValidationError illegalField(String fieldName, SchemaElementLocation path) {
            String msg = String.format("Illegal field found \"%s\"!", fieldName);
            return new ValidationError(msg, path);
        }

        public static ValidationError illegalType(ConfigNode node, ConfigNode.Type expected, SchemaElementLocation path) {
            String msg = String.format("Illegal node type found \"%s\", expected \"%s\"!", node.getType(), expected);
            return new ValidationError(msg, path);
        }

        public String getMessage() {
            return message;
        }
    }
}
