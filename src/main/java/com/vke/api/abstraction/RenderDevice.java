package com.vke.api.abstraction;

import com.vke.api.abstraction.commands.CommandBuffer;
import com.vke.api.abstraction.data.Buffer;
import com.vke.api.abstraction.data.Sampler;
import com.vke.api.abstraction.data.Texture;
import com.vke.api.abstraction.descriptors.BackendType;
import com.vke.api.abstraction.descriptors.DeviceCapabilities;
import com.vke.api.abstraction.descriptors.ShaderType;
import com.vke.api.abstraction.pipeline.ComputePipeline;
import com.vke.api.abstraction.pipeline.GraphicsPipeline;
import com.vke.api.abstraction.swapchain.Swapchain;
import com.vke.api.vulkan.pipeline.RenderPipeline;
import com.vke.api.vulkan.shaders.ShaderProgram;
import com.vke.utils.Disposable;
import com.vke.utils.Identifier;
import com.vke.utils.Pair;

import java.util.Map;

public interface RenderDevice extends Disposable {

    BackendType backend();
    DeviceCapabilities capabilities();

    /** MEMORY ALLOC **/
    Buffer createBuffer(Buffer.Description info);
    Texture createTexture(Texture.Description info);
    Sampler createSampler(Sampler.Description info);
    default ShaderProgram createShader(Identifier vertex)                      {   return new ShaderProgram(vertex);            }
    default ShaderProgram createShader(Identifier vertex, Identifier fragment) {   return new ShaderProgram(vertex, fragment);  }
    default ShaderProgram createShader(Pair<ShaderType, Identifier> shaders[]) {   return new ShaderProgram(shaders);           }
    default ShaderProgram createShader(Map<ShaderType, Identifier> shaders)    {   return new ShaderProgram(shaders);           }

    /** PIPELINE **/
    //GraphicsPipeline createRenderPipeline(RenderPipeline builder);
    //ComputePipeline createComputePipeline();

    /** COMMAND BUFFERS **/
    CommandBuffer createCommandBuffer();

    void submit(CommandBuffer cmd, CommandBuffer.SubmitInfo info);
    void waitIdle();

    /** SWAPCHAIN **/
    Swapchain createSwapchain(Swapchain.Description info);

}
