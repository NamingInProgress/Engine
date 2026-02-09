package com.vke.core.rendering.vulkan.pipeline;

import com.vke.api.vulkan.pipeline.PushConstantsDefinition;
import com.vke.api.vulkan.pipeline.RenderPipeline;
import com.vke.api.vulkan.shaders.ShaderProgram;
import com.vke.core.VKEngine;
import com.vke.core.rendering.vulkan.descriptor.DescriptorSetLayout;
import com.vke.core.rendering.vulkan.descriptor.DescriptorType;
import com.vke.core.rendering.vulkan.shader.Shader;
import com.vke.test.TestPushConstant;
import com.vke.utils.Identifier;
import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;

public class RenderPipelines {

    public static final RenderPipeline MAIN = VKEngine.REGISTRATE.pipeline("main")
            .topology(RenderPipeline.Topology.TRIANGLES)
            .withShader(new ShaderProgram(new Identifier("shaders/shader.vsh"), new Identifier("shaders/shader.fsh")))
            .cullMode(RenderPipeline.CullMode.NONE)
            .withColorAttachment(new RenderPipeline.ColorAttachmentInfo())
            .register();

    public static final RenderPipeline DEPTH_TEST = VKEngine.REGISTRATE.pipeline("depth_test")
            .topology(RenderPipeline.Topology.TRIANGLES)
            .withShader(new ShaderProgram(new Identifier("shaders/shader.vsh"), new Identifier("shaders/shader.fsh")))
            .cullMode(RenderPipeline.CullMode.NONE)
            .withColorAttachment(new RenderPipeline.ColorAttachmentInfo())
            .withDepthAttachment(new RenderPipeline.DepthStencilAttachmentInfo())
            .register();

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
            .withDescriptorLayout(new Identifier("shaders/test.layout.json"))
            //.addDescriptorSetLayout(new DescriptorSetLayout.Builder()
            //        .addBinding(0, DescriptorType.UniformBuffer, new Shader.Stages(Shader.Type.VERTEX)))
            .register();

    public static void init() {}

}
