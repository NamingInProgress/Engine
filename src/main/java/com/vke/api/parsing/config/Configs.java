package com.vke.api.parsing.config;

import com.vke.api.parsing.config.node.*;

import java.util.ArrayList;
import java.util.List;

public class Configs {
    public static String getString(ConfigNode object, String key) {
        ConfigObjectNode objectNode = (ConfigObjectNode) object;
        ConfigValueNode valueNode = (ConfigValueNode) objectNode.getNode(key);
        if (valueNode == null) return null;
        return valueNode.getValue();
    }

    public static float getNumber(ConfigNode object, String key) {
        ConfigObjectNode objectNode = (ConfigObjectNode) object;
        ConfigNumberNode valueNode = (ConfigNumberNode) objectNode.getNode(key);
        return valueNode.getValue();
    }

    public static Float getNumberSafe(ConfigNode object, String key) {
        ConfigObjectNode objectNode = (ConfigObjectNode) object;
        ConfigNumberNode valueNode = (ConfigNumberNode) objectNode.getNode(key);
        if (valueNode == null) return null;
        return valueNode.getValue();
    }

    public static int getInt(ConfigNode object, String key) {
        ConfigObjectNode objectNode = (ConfigObjectNode) object;
        ConfigNumberNode valueNode = (ConfigNumberNode) objectNode.getNode(key);
        return (int) valueNode.getValue();
    }

    public static Integer getIntSafe(ConfigNode object, String key) {
        ConfigObjectNode objectNode = (ConfigObjectNode) object;
        ConfigNumberNode valueNode = (ConfigNumberNode) objectNode.getNode(key);
        if (valueNode == null) return null;
        return (int) valueNode.getValue();
    }

    public static boolean getBoolean(ConfigNode object, String key) {
        ConfigObjectNode objectNode = (ConfigObjectNode) object;
        ConfigBooleanNode valueNode = (ConfigBooleanNode) objectNode.getNode(key);
        return valueNode.getValue();
    }

    public static Boolean getBooleanSafe(ConfigNode object, String key) {
        ConfigObjectNode objectNode = (ConfigObjectNode) object;
        ConfigBooleanNode valueNode = (ConfigBooleanNode) objectNode.getNode(key);
        if (valueNode == null) return null;
        return valueNode.getValue();
    }

    public static ConfigObjectNode getObject(ConfigNode object, String key) {
        ConfigObjectNode objectNode = (ConfigObjectNode) object;
        return (ConfigObjectNode) objectNode.getNode(key);
    }

    public static ConfigArrayNode getArray(ConfigNode object, String key) {
        ConfigObjectNode objectNode = (ConfigObjectNode) object;
        return (ConfigArrayNode) objectNode.getNode(key);
    }

    public static List<String> getStringList(ConfigArrayNode array) {
        List<String> list = new ArrayList<>(array.values().length);
        for (ConfigNode node : array.values()) {
            ConfigValueNode value = (ConfigValueNode) node;
            list.add(value.getValue());
        }
        return list;
    }
}