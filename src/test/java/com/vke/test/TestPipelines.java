package com.vke.test;

import com.vke.api.vulkan.pipeline.RenderPipeline;
import com.vke.api.vulkan.shaders.ShaderProgram;
import com.vke.core.VKEngine;
import com.vke.test.app.TestPushConstant;
import com.vke.utils.Identifier;

public class TestPipelines {

    public static final RenderPipeline IDK = VKEngine.REGISTRATE.pipeline("idk")
            .topology(RenderPipeline.Topology.TRIANGLES)
            .withShader(new ShaderProgram(new Identifier("shaders/idk.vsh"), new Identifier("shaders/idk.fsh")))
            .cullMode(RenderPipeline.CullMode.NONE)
            .withColorAttachment(new RenderPipeline.ColorAttachmentInfo()
                    .srcBlendFactor(RenderPipeline.BlendFactor.SRC_ALPHA)
                    .dstBlendFactor(RenderPipeline.BlendFactor.ONE_MINUS_SRC_ALPHA)
                    .colorBlendOp(RenderPipeline.BlendOperation.ADD)
                    .srcAlphaBlendFactor(RenderPipeline.BlendFactor.ONE)
                    .dstAlphaBlendFactor(RenderPipeline.BlendFactor.ZERO)
                    .alphaBlendOp(RenderPipeline.BlendOperation.ADD))
            .addPushConstants("vertexBufferPtr", new TestPushConstant())
            .withDescriptorLayout(new Identifier("shaders/idk.layout.json"))
            .register();

    public static void init() {}

}
