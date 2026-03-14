package com.vke.api.assets.pipeline.stages;

import com.vke.api.assets.pipeline.AssetPipelineException;
import com.vke.api.assets.pipeline.PipelineContext;
import com.vke.api.assets.pipeline.StageElement;
import com.vke.api.assets.pipeline.apis.ProtocolResolver;
import com.vke.api.parsing.config.node.ConfigArrayNode;
import com.vke.api.parsing.config.node.ConfigNode;

import java.net.URI;
import java.util.ArrayList;

public class RenameStage implements PipelineStage {
    public static final String STAGE = "rename";
    private final ArrayList<RenamePart> parts;

    public RenameStage(ConfigNode node, PipelineContext context) throws AssetPipelineException {
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
                        URI uri = URI.create(nodeContent);
                        ProtocolResolver<?> protocol = context.getResolver(uri.getScheme());
                        parts.add(new UriPart(uri, protocol));
                    } catch (IllegalArgumentException e) {
                        throw AssetPipelineException.illegalURI(STAGE, nodeContent, e.getMessage());
                    }
                }
                default -> throw new AssetPipelineException("Illegal rename part " + partName + "!");
            }
        }
    }

    @Override
    public void execute(StageElement stageElement, ExecutionTarget executionTarget) throws AssetPipelineException {
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
        protected abstract String applyForElement(StageElement element) throws AssetPipelineException;
    }

    private static non-sealed class StaticPart extends RenamePart {
        private final String staticString;

        private StaticPart(String staticString) {
            this.staticString = staticString;
        }

        @Override
        protected String applyForElement(StageElement element) throws AssetPipelineException {
            return staticString;
        }
    }

    private static non-sealed class UriPart extends RenamePart {
        private final URI uri;
        private final ProtocolResolver<?> protocol;

        private UriPart(URI uri, ProtocolResolver<?> protocol) {
            this.uri = uri;
            this.protocol = protocol;
        }

        @Override
        protected String applyForElement(StageElement element) throws AssetPipelineException {
            return protocol.resolveUri(uri, element);
        }
    }

}
