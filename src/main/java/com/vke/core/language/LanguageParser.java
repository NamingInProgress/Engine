package com.vke.core.language;

import com.vke.api.parsing.config.ConfigDocument;
import com.vke.api.parsing.config.node.ConfigNode;
import com.vke.api.parsing.config.node.ConfigObjectNode;
import com.vke.api.parsing.config.node.ConfigValueNode;
import com.vke.utils.io.SegmentedPath;

import java.util.Locale;
import java.util.Map;

public class LanguageParser {
    public static Language parseFromConfig(ConfigDocument document) {
        ConfigNode root = document.getRoot();
        ConfigObjectNode metaNode = root.getObject("meta");
        String langCode = metaNode.getString("lang");
        Locale locale = Locale.of(langCode);
        Language language = new Language(locale);

        ConfigObjectNode contentNode = root.getObject("content");
        handleObjectNode(contentNode, language, "");
        return language;
    }

    private static void handleObjectNode(ConfigObjectNode node, Language language, String path) {
        for (Map.Entry<String, ? extends ConfigNode> child : node.getDescendants().entrySet()) {
            ConfigNode childNode = child.getValue();
            String newPath = path.isEmpty() ? child.getKey() : path + "." + child.getKey();
            if (childNode instanceof ConfigValueNode valueNode) {
                language.setItem(new SegmentedPath(newPath), valueNode.getValue());
            } else if (childNode instanceof ConfigObjectNode objectNode) {
                handleObjectNode(objectNode, language, newPath);
            }
        }
    }
}
