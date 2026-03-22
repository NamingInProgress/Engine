package com.vke.core.assets.pipeline.protocols;

import com.vke.api.assets.Protocols;
import com.vke.api.parsing.config.ConfigDocument;
import com.vke.api.parsing.config.node.ConfigArrayNode;
import com.vke.api.parsing.config.node.ConfigNode;
import com.vke.api.parsing.config.node.ConfigObjectNode;
import com.vke.core.Context;
import com.vke.core.VKEngine;
import com.vke.core.assets.pipeline.AssetPipelineException;
import com.vke.core.assets.pipeline.Op;
import com.vke.core.assets.pipeline.apis.AssetData;
import com.vke.core.assets.pipeline.apis.AssetProtocol;
import com.vke.core.assets.pipeline.apis.AssetUri;
import com.vke.core.assets.pipeline.stages.PipelineStage;
import com.vke.utils.Utils;
import com.vke.utils.io.Identifier;
import com.vke.utils.io.SegmentedPath;

public class ConfigProtocol implements AssetProtocol<ConfigDocument> {
    @Override
    public String getProtocolName() {
        return Protocols.CONFIG;
    }

    @Override
    public AssetData getField(AssetData data, AssetUri uri) throws AssetPipelineException {
        if (uri.getSelector().equals("field")) {
            return AssetData.plain(getField(uri.getPath(), data.getDataAs()));
        } else {
            throw new AssetPipelineException("Illegal selector for protocol 'config': only 'field' is allowed!");
        }
    }

    private String getField(SegmentedPath path, ConfigDocument document) throws AssetPipelineException {
        String[] pathSegments = path.getParts();
        ConfigNode currentNode = document.getRoot();
        for (int i = 0; i < pathSegments.length; i++) {
            String segment = pathSegments[i];
            if (currentNode instanceof ConfigObjectNode objNode) {
                if (objNode.hasField(segment)) {
                    currentNode = objNode.getNode(segment);
                } else {
                    noSuchField(path);
                }
            } else if (currentNode instanceof ConfigArrayNode arrNode) {
                try {
                    int arrayIndex = Integer.parseInt(segment);
                    currentNode = arrNode.values()[arrayIndex];
                } catch (Exception e) {
                    noSuchField(path);
                }
            } else {
                noSuchField(path);
            }
        }

        String content = currentNode.asString();
        if (content == null) noSuchField(path);
        return content;
    }

    private void noSuchField(SegmentedPath path) throws AssetPipelineException {
        throw new AssetPipelineException("Field does not exist! " + path);
    }

    @Override
    public Loader getLoader() {
        return new ConfigProtocolLoader();
    }

    @Override
    public boolean applies(AssetData a, AssetData b, Op op) {
        return false;
    }

    public static class ConfigProtocolLoader implements Loader {
        @Override
        public AssetData load(Context context, Identifier identifier, PipelineStage.ExecutionTarget executionTarget) throws AssetPipelineException {
            return Utils.chainExceptions(() -> AssetData.config(ConfigDocument.parseIdentifier(identifier)));
        }
    }
}
