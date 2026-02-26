package com.vke.core.vulkan.pipeline;

import com.vke.api.assets.AssetHandle;
import com.vke.api.assets.r.R;
import com.vke.api.rendering.vulkan.pipeline.RenderPipeline;
import com.vke.api.rendering.vulkan.shaders.ShaderProgram;
import com.vke.core.VKEngine;
import com.vke.utils.Identifier;

public class RenderPipelines {

    public static final RenderPipeline MAIN = VKEngine.REGISTRATE.pipeline("main")
            .topology(RenderPipeline.Topology.TRIANGLES)
            .withShader(new ShaderProgram(new Identifier("shaders/shader.vsh"), new Identifier("shaders/shader.fsh")))
            .cullMode(RenderPipeline.CullMode.NONE)
            .withColorAttachment(new RenderPipeline.ColorAttachmentInfo())
            .register();

    public static final AssetHandle<RenderPipeline> IDK = R.pipelines.get("myMainPipeline");

    public static void init() {}

}
