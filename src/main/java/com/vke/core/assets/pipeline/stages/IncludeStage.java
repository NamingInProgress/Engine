package com.vke.core.assets.pipeline.stages;

import com.vke.api.parsing.config.ConfigDocument;
import com.vke.api.parsing.config.node.ConfigArrayNode;
import com.vke.api.parsing.config.node.ConfigNode;
import com.vke.core.assets.AssetException;
import com.vke.core.assets.pipeline.PipelineContext;
import com.vke.core.assets.pipeline.StageElement;
import com.vke.core.assets.service.AssetManagerScopedImpl;
import com.vke.utils.Utils;
import com.vke.utils.io.Identifier;

public class IncludeStage extends ParameterizedStage {
    public static final String STAGE = "include";
    private final CompoundPipelineStage inner;

    public IncludeStage(ConfigNode node, PipelineContext context) throws AssetException {
        super(node);
        String thingy = node.getString("file");
        Identifier ident = context.id(thingy);
        if (ident.existsFile()) {
            ConfigDocument document = Utils.chainExceptions(() -> AssetManagerScopedImpl.parseXml(ident));
            ConfigArrayNode rootNode = document.getRoot().asArray().values()[0].asArray();
            inner = new CompoundPipelineStage.Proxy(rootNode, context);
        } else {
            context.getEngine().explode();
            inner = null;
        }
    }

    @Override
    public void execute(StageElement stageElement, ExecutionTarget executionTarget) throws AssetException {
        inner.processInnerPipeline(stageElement, executionTarget);
    }
}
