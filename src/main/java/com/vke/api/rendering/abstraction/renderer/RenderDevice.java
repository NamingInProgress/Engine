package com.vke.api.rendering.abstraction.renderer;

import com.vke.api.rendering.abstraction.renderer.pipeline.ComputePipeline;
import com.vke.api.rendering.vulkan.pipeline.ComputePipelineData;
import com.vke.api.rendering.vulkan.pipeline.RenderPipelineData;
import com.vke.api.rendering.abstraction.renderer.commands.CommandBuffer;
import com.vke.api.rendering.abstraction.renderer.data.GpuBuffer;
import com.vke.api.rendering.abstraction.renderer.data.Sampler;
import com.vke.api.rendering.abstraction.renderer.data.Texture;
import com.vke.api.rendering.abstraction.renderer.enums.BackendType;
import com.vke.api.rendering.abstraction.renderer.enums.DeviceCapabilities;
import com.vke.api.rendering.abstraction.renderer.enums.ShaderType;
import com.vke.api.rendering.abstraction.renderer.pipeline.RenderPipeline;
import com.vke.api.rendering.abstraction.renderer.shader.Shader;
import com.vke.api.rendering.abstraction.renderer.swapchain.Swapchain;
import com.vke.utils.io.Disposable;
import com.vke.utils.io.Identifier;

import java.io.IOException;

public interface RenderDevice extends Disposable {

    BackendType backend();
    DeviceCapabilities capabilities();

    /** MEMORY ALLOC **/
    GpuBuffer createBuffer(GpuBuffer.Description info);
    Texture createTexture(Texture.TextureDesc info);
    Sampler createSampler(Sampler.Description info);
    Shader createShader(Identifier identifier, ShaderType type) throws IOException;

    /** PIPELINE **/
    RenderPipeline createRenderPipeline(RenderPipelineData data);
    ComputePipeline createComputePipeline(ComputePipelineData data);

    /** COMMAND BUFFERS **/
    CommandBuffer createCommandBuffer();

    void submit(CommandBuffer cmd, CommandBuffer.SubmitInfo info);
    void waitIdle();

    /** SWAPCHAIN **/
    Swapchain createSwapchain(Swapchain.Description info);

}
