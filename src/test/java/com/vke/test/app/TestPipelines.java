package com.vke.test.app;

import com.vke.api.rendering.vulkan.pipeline.RenderPipeline;
import com.vke.api.rendering.vulkan.shaders.ShaderProgram;
import com.vke.core.VKEngine;
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

    public static final RenderPipeline STH = VKEngine.REGISTRATE.pipeline("sth")
            .topology(RenderPipeline.Topology.TRIANGLES)
            .withShader(new ShaderProgram(new Identifier("shaders/sth.vsh"), new Identifier("shaders/sth.fsh")))
            .cullMode(RenderPipeline.CullMode.NONE)
            .withColorAttachment(new RenderPipeline.ColorAttachmentInfo()
                    .srcBlendFactor(RenderPipeline.BlendFactor.SRC_ALPHA)
                    .dstBlendFactor(RenderPipeline.BlendFactor.ONE_MINUS_SRC_ALPHA)
                    .colorBlendOp(RenderPipeline.BlendOperation.ADD)
                    .srcAlphaBlendFactor(RenderPipeline.BlendFactor.ONE)
                    .dstAlphaBlendFactor(RenderPipeline.BlendFactor.ZERO)
                    .alphaBlendOp(RenderPipeline.BlendOperation.ADD))
            .addPushConstants("vertexBufferPtr", new SthPushConstant())
            .withDescriptorLayout(new Identifier("shaders/sth.layout.json"))
            .register();

    public static void init() {}

}
