package com.vke.api.rendering.abstraction;

import com.vke.api.rendering.abstraction.commands.CommandBuffer;
import com.vke.api.rendering.abstraction.data.Buffer;
import com.vke.api.rendering.abstraction.data.Sampler;
import com.vke.api.rendering.abstraction.data.Texture;
import com.vke.api.rendering.abstraction.enums.BackendType;
import com.vke.api.rendering.abstraction.enums.DeviceCapabilities;
import com.vke.api.rendering.abstraction.enums.ShaderType;
import com.vke.api.rendering.abstraction.shader.Shader;
import com.vke.api.rendering.abstraction.swapchain.Swapchain;
import com.vke.api.rendering.vulkan.shaders.ShaderProgram;
import com.vke.core.file.png.Pixels;
import com.vke.utils.io.Disposable;
import com.vke.utils.io.Identifier;
import com.vke.utils.tuple.Pair;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.function.Consumer;

public interface RenderDevice extends Disposable {

    BackendType backend();
    DeviceCapabilities capabilities();

    /** MEMORY ALLOC **/
    Buffer createBuffer(Buffer.Description info);
    Texture createTexture(Pixels pixels, Texture.TextureDesc info);
    Sampler createSampler(Sampler.Description info);
    Shader createShader(Identifier identifier, ShaderType type) throws IOException;

    default ShaderProgram createShaders(Identifier vertex)                      {   return new ShaderProgram(vertex);            }
    default ShaderProgram createShaders(Identifier vertex, Identifier fragment) {   return new ShaderProgram(vertex, fragment);  }
    default ShaderProgram createShaders(Pair<ShaderType, Identifier>[] shaders) {   return new ShaderProgram(shaders);           }
    default ShaderProgram createShaders(Map<ShaderType, Identifier> shaders)    {   return new ShaderProgram(shaders);           }

    /** PIPELINE **/
    //GraphicsPipeline createRenderPipeline(RenderPipeline builder);
    //ComputePipeline createComputePipeline();

    /** COMMAND BUFFERS **/
    CommandBuffer createCommandBuffer();

    <T extends CommandBuffer> void submit(T cmd, CommandBuffer.SubmitInfo info);
    void waitIdle();

    /** SWAPCHAIN **/
    Swapchain createSwapchain(Swapchain.Description info);

}
