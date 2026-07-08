package com.vke.core.rendering.pipeline;

import com.vke.api.assets.AssetHandle;
import com.vke.api.assets.r.R;
import com.vke.api.rendering.abstraction.pipeline.Pipeline;
import com.vke.core.rendering.pipeline.driver.VertexConsumerPipelineDriver;

public class RenderPipelines {

    public static AssetHandle<? extends Pipeline> PIPELINE_VERTEX_CONSUMER = R.pipelines.get("vertex_consumer");

    public static VertexConsumerPipelineDriver VERTEX_CONSUMER = new VertexConsumerPipelineDriver(PIPELINE_VERTEX_CONSUMER);

}
