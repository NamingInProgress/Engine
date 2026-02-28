package com.vke.api.assets.pipeline;

import com.vke.api.parsing.config.node.ConfigNode;
import com.vke.api.assets.pipeline.stages.PipelineStage;

@FunctionalInterface
public interface StageFactory {
    PipelineStage produce(ConfigNode node, PipelineContext factories) throws AssetPipelineException;
}
