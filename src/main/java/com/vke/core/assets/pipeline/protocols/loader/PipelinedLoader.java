package com.vke.core.assets.pipeline.protocols.loader;

import com.vke.api.assets.AssetMeta;
import com.vke.core.Context;
import com.vke.core.assets.meta.AssetMetaAttributes;
import com.vke.core.assets.meta.AttributedAssetMeta;
import com.vke.core.assets.pipeline.AssetPipelinePhase;
import com.vke.core.assets.AssetException;
import com.vke.core.assets.pipeline.StageElement;
import com.vke.core.assets.pipeline.apis.AssetData;
import com.vke.core.assets.pipeline.apis.AssetProtocol;
import com.vke.core.assets.pipeline.stages.PipelineStage;
import com.vke.utils.io.Identifier;

public class PipelinedLoader implements AssetProtocol.Loader {
    private final AssetPipelinePhase pipeline;
    private final AssetMeta meta;

    public PipelinedLoader(AssetPipelinePhase pipeline, AssetMeta meta) {
        this.pipeline = pipeline;
        this.meta = meta;
    }

    @Override
    public AssetData load(Context context, Identifier identifier, PipelineStage.ExecutionTarget executionTarget) throws AssetException {
        String protocol = meta.getProtocol();
        AssetMetaAttributes attribs = new AssetMetaAttributes();
        if (meta instanceof AttributedAssetMeta attributed) {
            attribs = attributed.getAttributes();
        }
        StageElement element = new StageElement(identifier.toPath(), new AssetData(protocol, identifier), attribs);
        element.setAssetName(meta.getAssetName().getPath());
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
