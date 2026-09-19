package com.vke.api.parsing.config;

import com.vke.api.parsing.config.node.*;
import com.vke.core.parsing.config.utils.GeneralConfigArrayNode;
import org.jetbrains.annotations.Nullable;

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
        return valueNode.getNumber();
    }

    public static Float getNumberSafe(ConfigNode object, String key) {
        ConfigObjectNode objectNode = (ConfigObjectNode) object;
        ConfigNumberNode valueNode = (ConfigNumberNode) objectNode.getNode(key);
        if (valueNode == null) return null;
        return valueNode.getNumber();
    }

    public static int getInt(ConfigNode object, String key) {
        ConfigObjectNode objectNode = (ConfigObjectNode) object;
        ConfigNumberNode valueNode = (ConfigNumberNode) objectNode.getNode(key);
        return (int) valueNode.getNumber();
    }

    public static Integer getIntSafe(ConfigNode object, String key) {
        ConfigObjectNode objectNode = (ConfigObjectNode) object;
        ConfigNumberNode valueNode = (ConfigNumberNode) objectNode.getNode(key);
        if (valueNode == null) return null;
        return (int) valueNode.getNumber();
    }

    public static boolean getBoolean(ConfigNode object, String key) {
        ConfigObjectNode objectNode = (ConfigObjectNode) object;
        ConfigBooleanNode valueNode = (ConfigBooleanNode) objectNode.getNode(key);
        return valueNode.getBoolean();
    }

    public static Boolean getBooleanSafe(ConfigNode object, String key) {
        ConfigObjectNode objectNode = (ConfigObjectNode) object;
        ConfigBooleanNode valueNode = (ConfigBooleanNode) objectNode.getNode(key);
        if (valueNode == null) return null;
        return valueNode.getBoolean();
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

    public static boolean hasField(ConfigNode object, String key) {
        if (object instanceof ConfigObjectNode objectNode) {
            return objectNode.getNode(key) != null;
        }
        return false;
    }

    public static ConfigArrayNode mergeArrays(@Nullable ConfigArrayNode a, @Nullable ConfigArrayNode b) {
        if (a == null && b == null) return null;
        if (a == null) {
            return b;
        }
        if (b == null) {
            return a;
        }
        ConfigNode[] av = a.values();
        ConfigNode[] bv = b.values();
        int len = av.length + bv.length;
        ConfigNode[] arr = new ConfigNode[len];
        System.arraycopy(av, 0, arr, 0, av.length);
        System.arraycopy(bv, 0, arr, av.length, bv.length);
        return new GeneralConfigArrayNode(arr);
    }
}
