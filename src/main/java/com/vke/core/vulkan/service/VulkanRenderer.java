package com.vke.core.vulkan.service;

import com.vke.api.app.Framable;
import com.vke.api.assets.r.R;
import com.vke.api.rendering.FrameCounter;
import com.vke.api.rendering.abstraction.Renderer;
import com.vke.api.rendering.abstraction.commands.CommandBuffer;
import com.vke.api.rendering.abstraction.data.IFrameDataManager;
import com.vke.api.rendering.abstraction.data.ITextureManager;
import com.vke.api.rendering.abstraction.draw.VertexConsumerProvider;
import com.vke.api.rendering.abstraction.enums.QueueType;
import com.vke.api.rendering.abstraction.shader.Shader;
import com.vke.api.rendering.abstraction.swapchain.Swapchain;
import com.vke.api.rendering.vulkan.descriptors2.handles.UniformHandle;
import com.vke.api.rendering.vulkan.descriptors2.handles.buf.BufferHandle;
import com.vke.api.rendering.vulkan.descriptors2.handles.buf.FieldHandle;
import com.vke.api.rendering.vulkan.descriptors2.handles.other.CISHandle;
import com.vke.api.rendering.vulkan.descriptors2.handles.other.ImageHandle;
import com.vke.api.rendering.vulkan.descriptors2.handles.other.SamplerHandle;
import com.vke.api.rendering.vulkan.descriptors2.handles.other.array.CISArrayHandle;
import com.vke.api.rendering.vulkan.descriptors2.handles.other.array.ImageArrayHandle;
import com.vke.api.rendering.vulkan.descriptors2.handles.other.array.SamplerArrayHandle;
import com.vke.api.services2.ServiceImpl;
import com.vke.core.Context;
import com.vke.core.EngineCreateInfo;
import com.vke.core.VKEngine;
import com.vke.core.rendering.draw.FrameContext;
import com.vke.core.rendering.pipeline.RenderPipelines;
import com.vke.core.rendering.vertexconsumer.VulkanVertexConsumerProvider;
import com.vke.core.services2.Services;
import com.vke.core.vulkan.Scissor;
import com.vke.core.vulkan.Viewport;
import com.vke.core.vulkan.VulkanFrame;
import com.vke.core.vulkan.command.VulkanCmdBuffers;
import com.vke.core.vulkan.descriptor.EngineDescriptorSetsManager;
import com.vke.core.vulkan.device.VulkanRenderDevice;
import com.vke.core.vulkan.pipeline.VulkanPipelineLayout;
import com.vke.core.vulkan.sampler.Samplers;
import com.vke.core.vulkan.shr.service.ShaderReflector;
import com.vke.core.vulkan.swapchain.VulkanSwapchain;
import com.vke.core.vulkan.sync.VulkanFence;
import com.vke.core.vulkan.sync.VulkanSemaphore;
import com.vke.core.rendering.vertexconsumer.RecyclerArrayList;
import com.vke.core.window.Window;
import com.vke.utils.console.AnsiColors;
import com.vke.utils.tuple.Pair;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.KHRSwapchain;

import java.io.IOException;
import java.util.*;
import java.util.function.BiConsumer;

import static com.vke.core.VKEngine.profiler;

public class VulkanRenderer extends ServiceImpl implements Renderer {

    // Vulkan Stuff
    private VulkanSwapchain swapchain;
    private VulkanRenderDevice device;
    private VulkanFrame[] frames;
    private VulkanFence[] imagesInFlight;
    private VulkanSemaphore[] imagePresentInFlight;

    private VulkanFrame immediateFrame;

    private EngineDescriptorSetsManager engineSetsManager;

    // Engine infos
    private final VKEngine engine;
    private final Context context;
    private final EngineCreateInfo createInfo;
    private final FrameCounter frameCounter;

    private final VulkanVertexConsumerProvider vertexConsumerProvider;

    private int bindlessTexturesCount;

    public VulkanRenderer(Context context, EngineCreateInfo createInfo) {
        super(Services.VULKAN_RENDERER, context.getEngine());
        this.frameCounter = new FrameCounter(createInfo.vulkanCreateInfo.framesInFlight);
        this.engine = context.getEngine();
        this.context = context;
        this.createInfo = createInfo;
        this.vertexConsumerProvider = new VulkanVertexConsumerProvider(context, this);
    }

    @Override
    protected void onInitialize() {
        this.device = new VulkanRenderDevice(context, createInfo, this);
        this.bindlessTexturesCount = Math.min(device.capabilities().maxBindlessSampledImages, 8192);
        this.swapchain = device.createSwapchain(
                new Swapchain.Description(createInfo.vsync, engine.getWindow().getHandle()));
        this.imagesInFlight = new VulkanFence[this.swapchain.getImageCount()];
        this.imagePresentInFlight = new VulkanSemaphore[this.swapchain.getImageCount()];

        for (int i = 0; i < this.swapchain.getImageCount(); i++) {
            imagePresentInFlight[i] = VulkanSemaphore.createSemaphore(engine, device.getLogicalDevice());
        }

        Samplers.init(device);

        // VKE shader to set default descriptors via reflection instead of hard coding
        var temp = R.shaders.get("shaders/vke_sets.vsh");
        Shader s = null;
        try {
            s = temp.acquire(context);
        } catch (IOException e) {
            context.throwException(new IllegalStateException("Couldnt load shader vke_sets.vsh which is an engine internal shader and has to exist. -> Give up and die"), "VulkanRenderer#onInitialize");
        }
        engineSetsManager = new EngineDescriptorSetsManager(context, this, device,
                context.<ShaderReflector>service(Services.SHADER_REFLECTION).get(0)
                        .unwrapOrPanic(new IllegalStateException("Failed to find reflected shader for shader ID: 0")));
        s.free();
        engineSetsManager.ENGINE_PIPELINE_LAYOUT = VulkanPipelineLayout.getLayout(context.getEngine(), device, null,
                engineSetsManager.ENGINE_LAYOUTS.entrySet().stream()
                .sorted(Comparator.comparingInt(Map.Entry::getKey))
                .map(Map.Entry::getValue)
                .toList());
        engineSetsManager.makeFrameDataManager();

        this.immediateFrame = device.createImmediateFrame(swapchain);
        this.frames = device.createFrames(swapchain);
        RenderPipelines.init(context);
    }

    public FrameData startFrame(Window window, Framable f) {
        MemoryStack stack = MemoryStack.stackPush();

        VulkanFrame frame = frames[frameCounter.currentIndex()];
        VulkanFence fence = frame.getRenderFence();

        profiler.begin("Frame Fence");
        fence.waitForFence();
        profiler.end();

        profiler.begin("Image Acquire");
        int imageIndex = swapchain.acquireNextImage(frame.getImageSemaphore());
        if (imageIndex < 0 || imageIndex > imagesInFlight.length) {
            int errorCode = ~imageIndex;
            if (errorCode == KHRSwapchain.VK_ERROR_OUT_OF_DATE_KHR) {
                swapchain.recreate();
            }
            stack.close();
            profiler.closeStack();
            return null;
        }
        profiler.end();

        profiler.begin("Flight Fence");
        //if (imagesInFlight[imageIndex] != null) {
        //    imagesInFlight[imageIndex].waitForFence();
        //}

        fence.reset();

        imagesInFlight[imageIndex] = fence;
        profiler.end();

        profiler.begin("Cmd Buffers", AnsiColors.BLUE);
        profiler.begin("Get");
        VulkanCmdBuffers cmd = frame.getBuffers();
        cmd.reset();
        profiler.end();

        profiler.begin("Begin");
        cmd.begin();
        f.preRendering(new FrameContext(cmd, swapchain.getExtent(), window));
        cmd.beginRendering();
        profiler.end();
        profiler.end();

        int width = window.getSize().width();
        int height = window.getSize().height();

        Scissor sc = new Scissor(0, 0, width, height);
        Viewport wp = new Viewport(0, 0, width, height);

        cmd.setViewport(wp);
        cmd.setScissor(sc);

        FrameContext context = new FrameContext(cmd, swapchain.getExtent(), window);

        getEngineSetsManager().onFrame();

        return new FrameData(frame, stack, imageIndex, context);
    }

    public void endFrame(FrameData frameData, Framable f) {
        VulkanCmdBuffers cmd = frameData.frame().getBuffers();

        //VulkanPipelineLayout.LAYOUT_CACHE.values().forEach(layout ->
        //        layout.getGroup().getHandleCache().values().stream()
        //                .filter(e -> e instanceof BufferHandle)
        //                .forEach(uh -> ((BufferHandle) uh).nextFrame()));

        cmd.endRendering();
        f.postRendering(new FrameContext(cmd, swapchain.getExtent(), frameData.context().getWindow()));
        cmd.end();

        device.submit(cmd, new CommandBuffer.SubmitInfo(
                frameData.frame.getImageSemaphore(),
                imagePresentInFlight[frameData.imageIndex],
                frameData.frame.getRenderFence(),
                QueueType.GRAPHICS,
                false
        ));

        swapchain.present(imagePresentInFlight[frameData.imageIndex]);

        frameData.stack().close();
        this.frameCounter.nextFrame();
    }

    public void immediateSubmit(BiConsumer<MemoryStack, VulkanCmdBuffers> consumer) {
        this.immediateSubmit(consumer, () -> {});
    }

    public void immediateSubmit(BiConsumer<MemoryStack, VulkanCmdBuffers> consumer, Runnable finisher) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            VulkanFence fence = immediateFrame.getRenderFence();
            VulkanCmdBuffers cmd = immediateFrame.getBuffers();

            fence.reset();
            cmd.reset();

            cmd.beginImmediate();
            consumer.accept(stack, cmd);
            cmd.endImmediate();

            device.submit(cmd, CommandBuffer.SubmitInfo.immediate(fence));

            fence.waitForFence();
            finisher.run();
        }
    }

    @Override
    public List<String> dependencies() {
        return List.of(Services.SHADER_COMPILER, Services.ASSET_MANAGER);
    }

    @Override
    public VulkanRenderDevice getDevice() {
        return this.device;
    }

    @Override
    public FrameCounter getFrameCounter() {
        return this.frameCounter;
    }

    @Override
    public ITextureManager textureManager() {
        return getEngineSetsManager().textureManager;
    }

    @Override
    public IFrameDataManager frameDataManager() {
        return getEngineSetsManager().frameDataManager;
    }

    @Override
    public VertexConsumerProvider getVertexConsumerProvider() {
        return this.vertexConsumerProvider;
    }

    public EngineDescriptorSetsManager getEngineSetsManager() { return this.engineSetsManager; }

    public void scheduleDescriptorUpdate(VulkanPipelineLayout layout, UniformHandle handle) {
        getEngineSetsManager().scheduleDescriptorUpdate(layout, handle, frameCounter);
    }

    public int getBindlessTexturesCount() { return this.bindlessTexturesCount; }

    @Override
    public void free() {
        Samplers.free();
        // Pipelines get freed by the asset manager
        engineSetsManager.free();
        Arrays.stream(frames).forEach(VulkanFrame::free);
        Arrays.stream(imagePresentInFlight).forEach(VulkanSemaphore::free);
        this.immediateFrame.free();
        this.swapchain.free();
        this.device.free();
    }

    public record FrameData(VulkanFrame frame, MemoryStack stack, int imageIndex, FrameContext context) {}

    public static class IntWrapper {

        public IntWrapper() {
            anInt = 0;
        }

        public IntWrapper(int val) {
            anInt = val;
        }

        public int anInt;
    }

}
