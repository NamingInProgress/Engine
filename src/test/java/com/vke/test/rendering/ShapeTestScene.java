package com.vke.test.rendering;

import com.vke.api.assets.r.R;
import com.vke.api.draw.IVertexConsumer;
import com.vke.api.rendering.abstraction.pipeline.RenderPipeline;
import com.vke.api.rendering.vulkan.pushconstants.PushConstantHandle;
import com.vke.api.scene.Scene;
import com.vke.core.Context;
import com.vke.core.assets.handles.utils.LazyAssetHandle;
import com.vke.core.rendering.draw.DrawContext;
import com.vke.core.vulkan.command.VulkanCmdBuffers;
import com.vke.core.vulkan.pipeline.VulkanRenderPipeline;
import com.vke.utils.io.Identifier;

public class ShapeTestScene extends Scene {

    public ShapeTestScene(Identifier name, Context context) {
        super(name, context);
    }

    private LazyAssetHandle<RenderPipeline> DYNAMIC = R.pipelines.get("dynamic_vertices_test.pipeline.json");
    private VulkanRenderPipeline dynamicPipeline;

    private PushConstantHandle proj, transform;

    private IVertexConsumer<DynamicTestVertex> consumer;

    @Override
    public void onLoad() {
        dynamicPipeline = (VulkanRenderPipeline) DYNAMIC.assume(context);

        proj = dynamicPipeline.resolvePushConstant("world");
        transform = dynamicPipeline.resolvePushConstant("translation");
    }

    @Override
    public void onDraw(DrawContext ctx) {
        VulkanCmdBuffers cmd = (VulkanCmdBuffers) ctx.getCommandBuffer();

        cmd.bindPipeline(DYNAMIC);


    }

    @Override
    public void onUnload() {

    }

    @Override
    public void free() {

    }

}
