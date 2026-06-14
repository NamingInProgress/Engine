package com.vke.core.assets.pipeline.stages;

import com.vke.api.logger.LogLevel;
import com.vke.api.parsing.config.node.ConfigValueNode;
import com.vke.core.assets.AssetException;
import com.vke.core.assets.pipeline.PipelineContext;
import com.vke.core.assets.pipeline.StageElement;
import com.vke.api.parsing.config.node.ConfigArrayNode;
import com.vke.api.parsing.config.node.ConfigNode;
import com.vke.core.assets.pipeline.apis.AssetProtocol;
import com.vke.core.assets.pipeline.apis.AssetUri;
import com.vke.core.logger.LoggerFactory;

import java.net.URI;
import java.util.ArrayList;

//YES this class is literally a rebranded RenameStage LMAO cry about it
public class LogStage implements PipelineStage {
    public static final String STAGE = "log";

    private final LogLevel level;
    private final ArrayList<LogPart> parts;

    public LogStage(ConfigNode node, PipelineContext context) throws AssetException {
        this.level = LogLevel.valueOf(node.getStringOption("level").unwrapOr("INFO").toUpperCase());

        ConfigArrayNode partNodesNode = node.asArray();
        ConfigNode[] partNodes = partNodesNode.values();
        parts = new ArrayList<>(partNodes.length);
        for (ConfigNode partNode : partNodes) {
            //dont count attributes lol
            if (partNode instanceof ConfigValueNode) continue;
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
                default -> throw new AssetException("Illegal log part " + partName + "!");
            }
        }
    }

    @Override
    public void execute(StageElement stageElement, ExecutionTarget executionTarget) throws AssetException {
        StringBuilder nameBuilder = new StringBuilder();
        for (LogPart part : parts) {
            String p = part.applyForElement(stageElement);
            nameBuilder.append(p);
        }
        LoggerFactory.get("Asset Pipeline").log(level, nameBuilder.toString());
    }

    @Override
    public ExecutionTarget executionTarget() {
        return ExecutionTarget.Pseudo;
    }

    private static abstract sealed class LogPart permits StaticPart, UriPart {
        protected abstract String applyForElement(StageElement element) throws AssetException;
    }

    private static non-sealed class StaticPart extends LogPart {
        private final String staticString;

        private StaticPart(String staticString) {
            this.staticString = staticString;
        }

        @Override
        protected String applyForElement(StageElement element) throws AssetException {
            return staticString;
        }
    }

    private static non-sealed class UriPart extends LogPart {
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
