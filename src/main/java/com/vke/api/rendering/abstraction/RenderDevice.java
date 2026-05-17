package com.vke.api.rendering.abstraction;

import com.vke.api.rendering.abstraction.pipeline.ComputePipeline;
import com.vke.api.rendering.vulkan.pipeline.ComputePipelineData;
import com.vke.api.rendering.vulkan.pipeline.RenderPipelineData;
import com.vke.api.rendering.abstraction.commands.CommandBuffer;
import com.vke.api.rendering.abstraction.data.Buffer;
import com.vke.api.rendering.abstraction.data.Sampler;
import com.vke.api.rendering.abstraction.data.Texture;
import com.vke.api.rendering.abstraction.enums.BackendType;
import com.vke.api.rendering.abstraction.enums.DeviceCapabilities;
import com.vke.api.rendering.abstraction.enums.ShaderType;
import com.vke.api.rendering.abstraction.pipeline.RenderPipeline;
import com.vke.api.rendering.abstraction.shader.Shader;
import com.vke.api.rendering.abstraction.swapchain.Swapchain;
import com.vke.core.assets.pipeline.protocols.shader.ShaderPreprocessor;
import com.vke.core.file.png.Pixels;
import com.vke.utils.io.Disposable;
import com.vke.utils.io.Identifier;

import java.io.IOException;

public interface RenderDevice extends Disposable {

    BackendType backend();
    DeviceCapabilities capabilities();

    /** MEMORY ALLOC **/
    Buffer createBuffer(Buffer.Description info);
    Texture createTexture(Pixels pixels, Texture.TextureDesc info);
    Sampler createSampler(Sampler.Description info);
    Shader createShader(Identifier identifier, ShaderType type) throws IOException;

    /** PIPELINE **/
    RenderPipeline createRenderPipeline(RenderPipelineData data);
    ComputePipeline createComputePipeline(ComputePipelineData data);

    /** COMMAND BUFFERS **/
    CommandBuffer createCommandBuffer();

    <T extends CommandBuffer> void submit(T cmd, CommandBuffer.SubmitInfo info);
    void waitIdle();

    /** SWAPCHAIN **/
    Swapchain createSwapchain(Swapchain.Description info);

}
