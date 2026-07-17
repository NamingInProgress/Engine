package com.vke.core.assets.pipeline.stages;

import com.vke.api.logger.Logger;
import com.vke.api.parsing.config.node.ConfigArrayNode;
import com.vke.api.parsing.config.node.ConfigNode;
import com.vke.core.assets.AssetException;
import com.vke.core.assets.pipeline.PipelineContext;
import com.vke.core.assets.pipeline.StageElement;
import com.vke.core.assets.pipeline.apis.AssetCache;
import com.vke.core.assets.pipeline.apis.AssetData;
import com.vke.utils.io.Identifier;

public class CacheAssetStage extends CompoundPipelineStage {
    public static final String STAGE = "cache-asset";
    private final PipelineContext context;
    private final String forProtocol;

    public CacheAssetStage(ConfigNode node, PipelineContext context) throws AssetException {
        super(node.asArray(), context, "for");
        this.context = context;
        this.forProtocol = node.getString("for");
    }

    @Override
    public void execute(StageElement stageElement, ExecutionTarget executionTarget) throws AssetException {
        if (executionTarget == ExecutionTarget.Pseudo) {
            stageElement.setData(stageElement.getAssetData().reinterpret(forProtocol));
            return;
        }

        AssetCache cacheHandler = context.getCacheHandler(forProtocol);
        if (cacheHandler == null) {
            throw new AssetException("No AssetCache found for protocol " + forProtocol);
        }

        Identifier assetName = stageElement.getAssetName();
        AssetData maybeCached;
        try {
            maybeCached = cacheHandler.checkCache(context, assetName);
        } catch (Throwable e) {
            Logger logger = context.getLogger();
            logger.warn("Unable to load cache for asset %s: %s", assetName, e);
            logger.warn("Falling back to pipeline execution...", assetName, e);
            maybeCached = null;
        }
        if (maybeCached != null) {
            stageElement.setData(maybeCached);
        } else {
            processInnerPipeline(stageElement, executionTarget);
            AssetData producedData = stageElement.getAssetData();
            String producedProtocol = producedData.getProtocol();
            if (!producedProtocol.equals(forProtocol)) {
                throw new AssetException(String.format("Cache inner pipeline did not produce correct protocol (%s), got (%s)!", forProtocol, producedProtocol));
            }
            cacheHandler.cacheElement(context, stageElement, assetName);
        }
    }
}
