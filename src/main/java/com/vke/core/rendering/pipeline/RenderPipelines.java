package com.vke.core.rendering.pipeline;

import com.vke.api.assets.AssetHandle;
import com.vke.api.assets.r.R;
import com.vke.api.rendering.abstraction.RenderSystem;
import com.vke.api.rendering.abstraction.pipeline.Pipeline;
import com.vke.core.Context;
import com.vke.core.rendering.pipeline.driver.DemoPipelineDriver;
import com.vke.core.rendering.pipeline.driver.ShapePipelineDriver;

public class RenderPipelines {

    public static AssetHandle<? extends Pipeline> PIPELINE_DEMO = R.pipelines.get("demo.pipeline.json");
    public static AssetHandle<? extends Pipeline> PIPELINE_SHAPE = R.pipelines.get("load.pipeline.json");

    public static DemoPipelineDriver DEMO;
    public static ShapePipelineDriver LOAD;

    public static void init(RenderSystem context) {
        DEMO = new DemoPipelineDriver(context, PIPELINE_DEMO);
        LOAD = new ShapePipelineDriver(context, PIPELINE_SHAPE);
    }

}
