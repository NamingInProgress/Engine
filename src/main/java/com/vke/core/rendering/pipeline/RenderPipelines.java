package com.vke.core.rendering.pipeline;

import com.vke.api.assets.AssetHandle;
import com.vke.api.assets.r.R;
import com.vke.api.rendering.abstraction.renderer.RenderSystem;
import com.vke.api.rendering.abstraction.renderer.pipeline.Pipeline;
import com.vke.core.rendering.pipeline.driver.DeferredLightPassDriver;
import com.vke.core.rendering.pipeline.driver.DeferredPipelineDriver;
import com.vke.core.rendering.pipeline.driver.LoadPipelineDriver;
import com.vke.impl.rendering.driver.FullScreenDriver;
import com.vke.impl.rendering.driver.ShapePipelineDriver;

public class RenderPipelines {

    public static AssetHandle<? extends Pipeline> PIPELINE_DEFERRED = R.pipelines.get("deferred.pipeline.json");
    public static AssetHandle<? extends Pipeline> PIPELINE_DEFERRED_LIGHT = R.pipelines.get("light_pass.pipeline.json");
    public static AssetHandle<? extends Pipeline> PIPELINE_LOAD = R.pipelines.get("load.pipeline.json");

    public static DeferredPipelineDriver DEFERRED;
    public static DeferredLightPassDriver DEFERRED_LIGHT_PASS;
    public static LoadPipelineDriver LOAD;
    public static FullScreenDriver FULL_SCREEN;
    public static ShapePipelineDriver SHAPE;

    public static void init(RenderSystem context) {
        DEFERRED = new DeferredPipelineDriver(context, PIPELINE_DEFERRED);
        DEFERRED_LIGHT_PASS = new DeferredLightPassDriver(context, PIPELINE_DEFERRED_LIGHT);
        LOAD = new LoadPipelineDriver(context, PIPELINE_LOAD);
        FULL_SCREEN = new FullScreenDriver(context);
        SHAPE = new ShapePipelineDriver(context);
    }

}
