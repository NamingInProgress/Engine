package com.vke.api.assets.pipeline.parsers;

import com.vke.api.assets.pipeline.AssetPipelineException;
import com.vke.api.assets.pipeline.PipelineContext;
import com.vke.api.assets.pipeline.StageElement;
import com.vke.api.assets.pipeline.apis.AssetParser;
import com.vke.api.assets.pipeline.apis.ProtocolResolver;
import com.vke.api.parsing.config.ConfigDocument;
import com.vke.api.parsing.config.ConfigParser;
import com.vke.api.parsing.config.node.ConfigArrayNode;
import com.vke.utils.FileUtils;
import com.vke.utils.Identifier;

public class ConfigAssetParser implements AssetParser {
    public ConfigAssetParser(ConfigArrayNode arguments) {}

    @Override
    public String getResultingProtocol() {
        return "config";
    }

    @Override
    public void processStageElement(StageElement stageElement, PipelineContext context) throws AssetPipelineException {
        String protocol = stageElement.getProtocol();
        if (!"plain".equals(protocol)) {
            throw AssetPipelineException.incompatibleProtocol("parse", "plain", protocol);
        }
        Object maybeUnresolved = stageElement.getData();
        if (maybeUnresolved instanceof Identifier identifier) {
            stageElement.setData(getResultingProtocol(), identifier);
            return;
        }

        ProtocolResolver<String> resolver = context.getResolver("plain");
        String data = resolver.resolveData(stageElement);
        char[] source = data.toCharArray();
        String filename = FileUtils.getFileName(stageElement.getPath());
        ConfigParser parser = ConfigParser.forFileType(filename);
        if (parser == null) throw new AssetPipelineException("No suitable configparser found for " + filename);
        parser.setSource(source);
        try {
            ConfigDocument document = parser.parse(ConfigParser.PARSE_LITERALS | ConfigParser.ATTRIBS_TO_FIELDS);
            stageElement.setData(getResultingProtocol(), document);
        } catch (ConfigParser.ConfigParseException e) {
            throw new AssetPipelineException(e.getMessage());
        }
    }
}
