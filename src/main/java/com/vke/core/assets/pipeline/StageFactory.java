package com.vke.core.assets.pipeline;

import com.vke.api.parsing.config.node.ConfigNode;
import com.vke.core.assets.AssetException;
import com.vke.core.assets.pipeline.stages.PipelineStage;

@FunctionalInterface
public interface StageFactory {
    PipelineStage produce(ConfigNode node, PipelineContext factories) throws AssetException;
}
