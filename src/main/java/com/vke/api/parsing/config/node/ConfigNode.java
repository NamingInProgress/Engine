package com.vke.api.parsing.config.node;

import com.vke.api.parsing.config.Configs;

public interface ConfigNode {

    Type getType();

    default String getString(String key) {
        return Configs.getString(this, key);
    }

    default float getNumber(String key) {
        return Configs.getNumber(this, key);
    }

    default Float getNumberSafe(String key) {
        return Configs.getNumberSafe(this, key);
    }

    default int getInt(String key) {
        return Configs.getInt(this, key);
    }

    default Integer getIntSafe(String key) {
        return Configs.getIntSafe(this, key);
    }

    default boolean getBoolean(String key) {
        return Configs.getBoolean(this, key);
    }

    default Boolean getBooleanSafe(String key) {
        return Configs.getBooleanSafe(this, key);
    }

    default ConfigObjectNode getObject(String key) {
        return Configs.getObject(this, key);
    }

    default ConfigArrayNode getArray(String key) {
        return Configs.getArray(this, key);
    }

    default boolean hasField(String key) {
        if (this instanceof ConfigObjectNode objectNode) {
            return objectNode.getNode(key) != null;
        }
        return false;
    }

    enum Type {
        Object,
        Array,
        Number,
        Value,
        Boolean,
        Meta,
    }
}
