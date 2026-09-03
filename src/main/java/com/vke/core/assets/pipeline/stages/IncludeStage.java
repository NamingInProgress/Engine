package com.vke.core.assets.pipeline.stages;

import com.vke.api.parsing.config.ConfigDocument;
import com.vke.api.parsing.config.node.ConfigArrayNode;
import com.vke.api.parsing.config.node.ConfigNode;
import com.vke.core.FileIdentifier;
import com.vke.core.assets.AssetException;
import com.vke.core.assets.pipeline.PipelineContext;
import com.vke.core.assets.pipeline.StageElement;
import com.vke.core.assets.service.AssetManagerScopedImpl;
import com.vke.utils.Utils;

public class IncludeStage extends ParameterizedStage {
    public static final String STAGE = "include";
    private final CompoundPipelineStage inner;

    public IncludeStage(ConfigNode node, PipelineContext context) throws AssetException {
        super(node);
        String thingy = node.getString("file");
        FileIdentifier ident = context.fid(thingy);
        if (ident.existsFile()) {
            ConfigDocument document = Utils.chainExceptions(() -> ConfigDocument.parseIdentifier(ident));
            ConfigArrayNode rootNode = document.getRoot().asArray().values()[0].asArray();
            inner = new CompoundPipelineStage.Proxy(rootNode, context);
        } else {
            inner = null;
            throw new AssetException("Failed to find " + ident + " in assets!");
        }
    }

    @Override
    public void execute(StageElement stageElement, ExecutionTarget executionTarget) throws AssetException {
        inner.processInnerPipeline(stageElement, executionTarget);
    }
}
