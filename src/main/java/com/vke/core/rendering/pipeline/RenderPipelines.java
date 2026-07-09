package com.vke.core.rendering.pipeline;

import com.vke.api.assets.AssetHandle;
import com.vke.api.assets.r.R;
import com.vke.api.rendering.abstraction.pipeline.Pipeline;
import com.vke.core.Context;
import com.vke.core.rendering.pipeline.driver.DemoPipelineDriver;
import com.vke.core.rendering.pipeline.driver.VertexConsumerPipelineDriver;

public class RenderPipelines {

    public static AssetHandle<? extends Pipeline> PIPELINE_VERTEX_CONSUMER = R.pipelines.get("vertex_consumer.pipeline.json");
    public static AssetHandle<? extends Pipeline> PIPELINE_DEMO = R.pipelines.get("demo.pipeline.json");

    public static VertexConsumerPipelineDriver VERTEX_CONSUMER;
    public static DemoPipelineDriver DEMO;

    public static void init(Context context) {
        VERTEX_CONSUMER = new VertexConsumerPipelineDriver(PIPELINE_VERTEX_CONSUMER);
        DEMO = new DemoPipelineDriver(context, PIPELINE_DEMO);
    }

}
