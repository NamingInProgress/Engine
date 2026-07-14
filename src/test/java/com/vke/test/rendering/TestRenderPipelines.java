package com.vke.test.rendering;

import com.vke.api.assets.AssetHandle;
import com.vke.api.assets.r.R;
import com.vke.api.rendering.abstraction.renderer.RenderSystem;
import com.vke.api.rendering.abstraction.renderer.pipeline.Pipeline;
import com.vke.test.rendering.instancing.InstancingTestDriver;

public class TestRenderPipelines {

    public static AssetHandle<? extends Pipeline> PIPELINE_INSTANCING = R.pipelines.get("instancing_test.pipeline.json");

    public static InstancingTestDriver INSTANCING;

    public static void init(RenderSystem ctx) {
        INSTANCING = new InstancingTestDriver(ctx, PIPELINE_INSTANCING);
    }

}
