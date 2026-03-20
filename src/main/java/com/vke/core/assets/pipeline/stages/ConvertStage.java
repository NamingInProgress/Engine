package com.vke.core.assets.pipeline.stages;

import com.vke.core.assets.pipeline.PipelineContext;
import com.vke.api.parsing.config.node.ConfigNode;
import com.vke.core.assets.pipeline.AssetPipelineException;
import com.vke.core.assets.pipeline.StageElement;
import com.vke.core.assets.pipeline.apis.AssetData;
import com.vke.core.assets.pipeline.apis.AssetProtocol;
import com.vke.core.assets.pipeline.apis.AssetConverter;

public class ConvertStage extends ParameterizedStage {
    public static final String STAGE = "convert";
    private final PipelineContext context;
    private final String toName;

    public ConvertStage(ConfigNode node, PipelineContext context) throws AssetPipelineException {
        super(node);
        this.context = context;
        this.toName = node.getString("to");
    }

    @Override
    public void execute(StageElement stageElement, ExecutionTarget executionTarget) throws AssetPipelineException {
        if (executionTarget == ExecutionTarget.Pseudo) {
            stageElement.setData(stageElement.getAssetData().reinterpret(toName));
            return;
        }
        String fromName = stageElement.getProtocol();
        AssetConverter converter = context.getConverter(fromName, toName);
        if (converter == null) throw new AssetPipelineException("There is no converter from '%s' to '%s'!".formatted(fromName, toName));
        if (!stageElement.getAssetData().isResolved()) {
            //now we have to resolve the asset actually
            AssetProtocol<?> protocol = context.getProtocol(fromName);
            AssetProtocol.Loader loader = protocol.getLoader();
            AssetData resolved = loader.load(context.engine(), stageElement.getAssetData().getUnresolved(), executionTarget);
            stageElement.setData(resolved);
        }
        stageElement.setData(converter.performConversion(stageElement, arguments));
    }

    @Override
    public ExecutionTarget executionTarget() {
        return ExecutionTarget.All;
    }
}
