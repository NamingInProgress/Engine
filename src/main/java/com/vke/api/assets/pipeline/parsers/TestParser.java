package com.vke.api.assets.pipeline.parsers;

import com.vke.api.assets.pipeline.AssetPipelineException;
import com.vke.api.assets.pipeline.PipelineContext;
import com.vke.api.assets.pipeline.StageElement;
import com.vke.api.assets.pipeline.apis.AssetParser;
import com.vke.api.parsing.config.node.ConfigArrayNode;

public class TestParser implements AssetParser {
    public TestParser(ConfigArrayNode arguments) {}

    @Override
    public String getResultingProtocol() {
        return "plain";
    }

    @Override
    public void processStageElement(StageElement stageElement, PipelineContext context) throws AssetPipelineException {
        String protocol = stageElement.getProtocol();
        if ("config".equals(protocol)) {
            stageElement.setData("plain", "Hello World!");
            return;
        }

        throw AssetPipelineException.incompatibleProtocol("parse", "config", protocol);
    }
}
