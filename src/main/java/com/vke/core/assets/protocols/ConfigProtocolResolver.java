package com.vke.core.assets.protocols;

import com.vke.api.assets.AssetHandle;
import com.vke.api.assets.pipeline.AssetPipelineException;
import com.vke.api.assets.pipeline.StageElement;
import com.vke.api.assets.pipeline.StageFilter;
import com.vke.api.assets.pipeline.apis.ProtocolResolver;
import com.vke.api.parsing.config.ConfigDocument;
import com.vke.api.parsing.config.node.ConfigArrayNode;
import com.vke.api.parsing.config.node.ConfigNode;
import com.vke.api.parsing.config.node.ConfigObjectNode;
import com.vke.core.assets.handles.ConfigAssetHandle;
import com.vke.utils.Identifier;

import java.io.IOException;

public class ConfigProtocolResolver implements ProtocolResolver<ConfigDocument> {
    @Override
    public boolean checkProtocolContent(StageFilter filter, StageElement stageElement) throws AssetPipelineException {
        String selector = filter.getSelector();
        return switch (selector) {
            case "field" -> checkField(filter, stageElement);
            default -> throw AssetPipelineException.unknownSelector("meta", selector);
        };
    }

    @Override
    public AssetHandle<?> createHandle(StageElement element) throws AssetPipelineException {
        String protocol = element.getProtocol();
        Object data = element.getData();
        if ("config".equals(protocol)) {
            //there are two possibilities here:
            if (data instanceof Identifier identifier) {
                //1. this is unresolved (an identifier)
                return new ConfigAssetHandle(identifier);
            } else if (data instanceof ConfigDocument doc){
                //2. this is resolved
                return new ConfigAssetHandle(doc);
            }
        }

        throw new AssetPipelineException("Cannot create AssetHandle from StageElement!");
    }

    @Override
    public ConfigDocument resolveData(StageElement element) throws AssetPipelineException {
        String protocol = element.getProtocol();
        Object data = element.getData();
        if ("config".equals(protocol)) {
            //there are two possibilities here:
            if (data instanceof Identifier identifier) {
                //1. this is unresolved (an identifier)
                try {
                    return ConfigDocument.parseIdentifier(identifier);
                } catch (IOException e) {
                    throw new AssetPipelineException(e);
                }
            } else if (data instanceof ConfigDocument doc){
                //2. this is resolved
                return doc;
            }
        }

        throw new AssetPipelineException("Cannot resolve data from StageElement!");
    }

    private boolean checkField(StageFilter filter, StageElement element) throws AssetPipelineException {
        ConfigDocument document = resolveData(element);
        String path = filter.getPath();
        String[] pathSegments = path.split("/");
        ConfigNode currentNode = document.getRoot();
        for (int i = 0; i < pathSegments.length; i++) {
            String segment = pathSegments[i];
            if (currentNode instanceof ConfigObjectNode objNode) {
                if (objNode.hasField(segment)) {
                    currentNode = objNode.getNode(segment);
                } else {
                    return false;
                }
            } else if (currentNode instanceof ConfigArrayNode arrNode) {
                try {
                    int arrayIndex = Integer.parseInt(segment);
                    currentNode = arrNode.values()[arrayIndex];
                } catch (Exception e) {
                    return false;
                }
            } else {
                return false;
            }
        }

        String content = currentNode.asString();
        if (content == null) return false;
        return filter.applyForString(content);
    }
}
