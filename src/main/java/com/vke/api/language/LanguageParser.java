package com.vke.api.language;

import com.vke.api.assets.pipeline.AssetPipelineException;
import com.vke.api.assets.pipeline.PipelineContext;
import com.vke.api.assets.pipeline.StageElement;
import com.vke.api.assets.pipeline.apis.AssetParser;
import com.vke.api.assets.pipeline.apis.ProtocolResolver;
import com.vke.api.assets.pipeline.stages.ParseStage;
import com.vke.api.parsing.config.ConfigDocument;
import com.vke.api.parsing.config.node.ConfigArrayNode;
import com.vke.api.parsing.config.node.ConfigNode;
import com.vke.api.parsing.config.node.ConfigObjectNode;
import com.vke.api.parsing.config.node.ConfigValueNode;
import com.vke.utils.Identifier;
import com.vke.utils.Location;

import java.nio.file.Paths;
import java.util.Locale;
import java.util.Map;

public class LanguageParser implements AssetParser {
    public LanguageParser(ConfigArrayNode arguments) {
    }

    @Override
    public String getResultingProtocol() {
        return "lang";
    }

    @Override
    public void processStageElement(StageElement stageElement, PipelineContext context) throws AssetPipelineException {
        String prot = stageElement.getProtocol();
        if ("config".equals(prot)) {
            Object data = stageElement.getData();
            if (data instanceof Identifier ident) {
                stageElement.setData("lang", ident);
                return;
            }
            ProtocolResolver<ConfigDocument> resolver = context.getResolver("config");
            ConfigDocument document = resolver.resolveData(stageElement);
            Language language = parseFromConfig(document);
            stageElement.setData("lang", language);
        } else {
            throw AssetPipelineException.incompatibleProtocol(ParseStage.STAGE, "config", prot);
        }
    }

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
                language.setItem(new Location(newPath), valueNode.getValue());
            } else if (childNode instanceof ConfigObjectNode objectNode) {
                handleObjectNode(objectNode, language, newPath);
            }
        }
    }
}
