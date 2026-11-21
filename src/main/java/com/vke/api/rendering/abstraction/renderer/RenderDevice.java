package com.vke.api.rendering.abstraction.renderer;

import com.vke.api.rendering.abstraction.renderer.commands.ImmediateSubmit;
import com.vke.api.rendering.abstraction.renderer.data.IRenderAllocator;
import com.vke.api.rendering.abstraction.renderer.enums.QueueType;
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
import com.vke.core.FileIdentifier;
import com.vke.core.image.Bitmap;
import com.vke.core.rendering.vulkan.texture.VulkanTexture;
import com.vke.utils.io.Disposable;

import java.io.IOException;
import java.nio.ByteBuffer;

public interface RenderDevice extends Disposable {

    BackendType backend();
    DeviceCapabilities capabilities();

    /** MEMORY ALLOC **/
    GpuBuffer createBuffer(GpuBuffer.Description info);
    Texture createTexture(Texture.TextureDesc info);
    Sampler createSampler(Sampler.Description info);
    Shader createShader(FileIdentifier identifier, ShaderType type) throws IOException;

    ByteBuffer allocateManaged(int size);
    ByteBuffer allocate(int size);

    void freeAllocation(ByteBuffer buffer);

    IRenderAllocator allocator();

    /** PIPELINE **/
    RenderPipeline createRenderPipeline(RenderPipelineData data);
    ComputePipeline createComputePipeline(ComputePipelineData data);

    /** IMMEDIATE **/
    ImmediateSubmit immediateSubmit(QueueType qt, boolean useSignalSemaphore, boolean useFence);

    void submit(CommandBuffer cmd, CommandBuffer.SubmitInfo info);
    void waitIdle();

    /** SWAPCHAIN **/
    Swapchain createSwapchain(Swapchain.Description info);

    /** OVERLOADS **/
    default ImmediateSubmit immediateSubmit() {
        return immediateSubmit(QueueType.TRANSFER);
    }
    default ImmediateSubmit immediateSubmit(QueueType qt) {
        return immediateSubmit(qt, false);
    }
    default ImmediateSubmit immediateSubmit(QueueType qt, boolean useSignalSemaphore) {
        return immediateSubmit(qt, useSignalSemaphore, true);
    }

    default GpuBuffer createBufferManaged(GpuBuffer.Description info) {
        return createBufferManaged(allocator(), info);
    }

    default Texture createTextureManaged(Texture.TextureDesc info) {
        return createTextureManaged(allocator(), info);
    }

    default Sampler createSamplerManaged(Sampler.Description info) {
        return createSamplerManaged(allocator(), info);
    }

    default Shader createShaderManaged(FileIdentifier identifier, ShaderType type) throws IOException {
        return createShaderManaged(allocator(), identifier, type);
    }

    default GpuBuffer createBufferManaged(IRenderAllocator allocator, GpuBuffer.Description info) {
        var buf = createBuffer(info);
        allocator.allocate(buf);
        return buf;
    }

    default Texture createTextureManaged(IRenderAllocator allocator, Texture.TextureDesc info) {
        var buf = createTexture(info);
        allocator.allocate(buf);
        return buf;
    }

    default Sampler createSamplerManaged(IRenderAllocator allocator, Sampler.Description info) {
        var buf = createSampler(info);
        allocator.allocate(buf);
        return buf;
    }

    default Shader createShaderManaged(IRenderAllocator allocator, FileIdentifier identifier, ShaderType type) throws IOException {
        var buf = createShader(identifier, type);
        allocator.allocate(buf);
        return buf;
    }

}
