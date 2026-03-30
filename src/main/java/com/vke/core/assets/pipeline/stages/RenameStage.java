package com.vke.core.assets.pipeline.stages;

import com.vke.core.assets.AssetException;
import com.vke.core.assets.pipeline.PipelineContext;
import com.vke.core.assets.pipeline.StageElement;
import com.vke.api.parsing.config.node.ConfigArrayNode;
import com.vke.api.parsing.config.node.ConfigNode;
import com.vke.core.assets.pipeline.apis.AssetProtocol;
import com.vke.core.assets.pipeline.apis.AssetUri;

import java.net.URI;
import java.util.ArrayList;

public class RenameStage implements PipelineStage {
    public static final String STAGE = "rename";
    private final ArrayList<RenamePart> parts;

    public RenameStage(ConfigNode node, PipelineContext context) throws AssetException {
        ConfigArrayNode partNodesNode = node.asArray();
        ConfigNode[] partNodes = partNodesNode.values();
        parts = new ArrayList<>(partNodes.length);
        for (ConfigNode partNode : partNodes) {
            String partName = partNode.getNodeName();
            String nodeContent = partNode.asArray().values()[0].asString();
            switch (partName) {
                case "static-part" -> parts.add(new StaticPart(nodeContent));
                case "uri-part" -> {
                    try {
                        AssetUri uri = new AssetUri(URI.create(nodeContent));
                        AssetProtocol<?> protocol = context.getProtocol(uri.getProtocol());
                        parts.add(new UriPart(uri, protocol));
                    } catch (IllegalArgumentException e) {
                        throw AssetException.illegalURI(STAGE, nodeContent, e.getMessage());
                    }
                }
                default -> throw new AssetException("Illegal rename part " + partName + "!");
            }
        }
    }

    @Override
    public void execute(StageElement stageElement, ExecutionTarget executionTarget) throws AssetException {
        StringBuilder nameBuilder = new StringBuilder();
        for (RenamePart part : parts) {
            String p = part.applyForElement(stageElement);
            nameBuilder.append(p);
        }
        stageElement.setAssetName(nameBuilder.toString());
    }

    @Override
    public ExecutionTarget executionTarget() {
        return ExecutionTarget.Pseudo;
    }

    private static abstract sealed class RenamePart permits StaticPart, UriPart {
        protected abstract String applyForElement(StageElement element) throws AssetException;
    }

    private static non-sealed class StaticPart extends RenamePart {
        private final String staticString;

        private StaticPart(String staticString) {
            this.staticString = staticString;
        }

        @Override
        protected String applyForElement(StageElement element) throws AssetException {
            return staticString;
        }
    }

    private static non-sealed class UriPart extends RenamePart {
        private final AssetUri uri;
        private final AssetProtocol<?> protocol;

        private UriPart(AssetUri uri, AssetProtocol<?> protocol) {
            this.uri = uri;
            this.protocol = protocol;
        }

        @Override
        protected String applyForElement(StageElement element) throws AssetException {
            return protocol.getField(element.getAssetData(uri.getProtocol()), uri).getData().toString();
        }
    }

}
