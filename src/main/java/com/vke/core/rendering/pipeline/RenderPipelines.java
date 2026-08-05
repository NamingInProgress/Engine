package com.vke.core.rendering.pipeline;

import com.vke.api.assets.AssetHandle;
import com.vke.api.assets.r.R;
import com.vke.api.rendering.abstraction.renderer.RenderSystem;
import com.vke.api.rendering.abstraction.renderer.pipeline.Pipeline;
import com.vke.core.rendering.pipeline.driver.DeferredLightPassDriver;
import com.vke.core.rendering.pipeline.driver.DeferredPipelineDriver;
import com.vke.core.rendering.pipeline.driver.ShapePipelineDriver;
import com.vke.impl.driver.FullScreenDriver;

public class RenderPipelines {

    public static AssetHandle<? extends Pipeline> PIPELINE_DEFERRED = R.pipelines.get("deferred.pipeline.json");
    public static AssetHandle<? extends Pipeline> PIPELINE_DEFERRED_LIGHT = R.pipelines.get("light_pass.pipeline.json");
    public static AssetHandle<? extends Pipeline> PIPELINE_SHAPE = R.pipelines.get("load.pipeline.json");

    public static DeferredPipelineDriver DEFERRED;
    public static DeferredLightPassDriver DEFERRED_LIGHT_PASS;
    public static ShapePipelineDriver LOAD;
    public static FullScreenDriver FULL_SCREEN;

    public static void init(RenderSystem context) {
        DEFERRED = new DeferredPipelineDriver(context, PIPELINE_DEFERRED);
        DEFERRED_LIGHT_PASS = new DeferredLightPassDriver(context, PIPELINE_DEFERRED_LIGHT);
        LOAD = new ShapePipelineDriver(context, PIPELINE_SHAPE);
        FULL_SCREEN = new FullScreenDriver(context);
    }

}
