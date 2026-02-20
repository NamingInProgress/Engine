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

    default String getNodeName() {
        if (this instanceof NamedConfigNode named) {
            return named.getName();
        }
        return null;
    }

    default boolean hasField(String key) {
        if (this instanceof ConfigObjectNode objectNode) {
            return objectNode.getNode(key) != null;
        }
        return false;
    }

    default ConfigObjectNode asObject() {
        if (this instanceof ConfigObjectNode valueNode) return valueNode;
        return null;
    }

    default ConfigArrayNode asArray() {
        if (this instanceof ConfigArrayNode valueNode) return valueNode;
        return null;
    }

    default String asString() {
        if (this instanceof ConfigValueNode valueNode) return valueNode.getValue();
        return null;
    }
    default float asNumber() {
        if (this instanceof ConfigNumberNode valueNode) return valueNode.getValue();
        return 0;
    }

    default boolean asBoolean() {
        if (this instanceof ConfigBooleanNode valueNode) return valueNode.getValue();
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
