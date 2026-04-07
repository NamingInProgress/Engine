package com.vke.core.assets.pipeline.protocols.loader;

import com.vke.core.Context;
import com.vke.core.assets.pipeline.AssetPipeline;
import com.vke.core.assets.AssetException;
import com.vke.core.assets.pipeline.StageElement;
import com.vke.core.assets.pipeline.apis.AssetData;
import com.vke.core.assets.pipeline.apis.AssetProtocol;
import com.vke.core.assets.pipeline.stages.PipelineStage;
import com.vke.utils.io.Identifier;

public class PipelinedLoader implements AssetProtocol.Loader {
    private final AssetPipeline pipeline;
    private final String protocol;

    public PipelinedLoader(AssetPipeline pipeline, String protocol) {
        this.pipeline = pipeline;
        this.protocol = protocol;
    }

    @Override
    public AssetData load(Context context, Identifier identifier, PipelineStage.ExecutionTarget executionTarget) throws AssetException {
        StageElement element = new StageElement(identifier.toPath(), new AssetData(protocol, identifier));
        pipeline.execute(element, executionTarget);
        AssetData data = element.getAssetData();
        if (!data.isResolved()) {
            AssetProtocol<?> assetProtocol = pipeline.getContext().getProtocol(data.getProtocol());
            AssetData resolved = assetProtocol.getLoader().load(context, identifier, executionTarget);
            element.setData(resolved);
        }
        return element.getAssetData();
    }
}
