package com.vke.core.rendering.vulkan.pipeline;

import com.vke.api.vulkan.pipeline.RenderPipeline;
import com.vke.api.vulkan.shaders.ShaderProgram;
import com.vke.core.VKEngine;
import com.vke.utils.Identifier;

public class RenderPipelines {

    public static final RenderPipeline MAIN = VKEngine.VKE_REGISTRATE.pipeline("main")
            .topology(RenderPipeline.Topology.TRIANGLES)
            .withShader(new ShaderProgram(new Identifier("shaders/shader.vsh"), new Identifier("shaders/shader.fsh")))
            .cullMode(RenderPipeline.CullMode.NONE)
            .withColorAttachment(new RenderPipeline.ColorAttachmentInfo())
            .register();

    public static final RenderPipeline DEPTH_TEST = VKEngine.VKE_REGISTRATE.pipeline("depth_test")
            .topology(RenderPipeline.Topology.TRIANGLES)
            .withShader(new ShaderProgram(new Identifier("shaders/shader.vsh"), new Identifier("shaders/shader.fsh")))
            .cullMode(RenderPipeline.CullMode.NONE)
            .withColorAttachment(new RenderPipeline.ColorAttachmentInfo())
            .withDepthAttachment(new RenderPipeline.DepthStencilAttachmentInfo())
            .register();

    public static void init() {}

}
