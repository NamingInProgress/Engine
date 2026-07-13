package com.vke.core.vulkan.service;

import com.vke.api.app.Framable;
import com.vke.api.assets.r.R;
import com.vke.api.rendering.FrameCounter;
import com.vke.api.rendering.abstraction.Renderer;
import com.vke.api.rendering.abstraction.commands.CommandBuffer;
import com.vke.api.rendering.abstraction.draw.VertexConsumerProvider;
import com.vke.api.rendering.abstraction.enums.QueueType;
import com.vke.api.rendering.abstraction.shader.Shader;
import com.vke.api.rendering.abstraction.swapchain.Swapchain;
import com.vke.api.rendering.vulkan.descriptors.bindings.BufferBinding;
import com.vke.api.rendering.vulkan.descriptors2.handles.UniformHandle;
import com.vke.api.rendering.vulkan.descriptors2.handles.buf.BufferHandle;
import com.vke.api.services2.ServiceImpl;
import com.vke.core.Context;
import com.vke.core.EngineCreateInfo;
import com.vke.core.VKEngine;
import com.vke.core.rendering.pipeline.RenderPipelines;
import com.vke.core.rendering.vertexconsumer.VulkanVertexConsumerProvider;
import com.vke.core.services2.Services;
import com.vke.core.vulkan.Scissor;
import com.vke.core.vulkan.Viewport;
import com.vke.core.vulkan.VulkanFrame;
import com.vke.core.vulkan.command.VulkanCmdBuffers;
import com.vke.core.vulkan.data.VulkanResourceManager;
import com.vke.core.vulkan.descriptor.EngineDescriptorSetsManager;
import com.vke.core.vulkan.device.VulkanRenderDevice;
import com.vke.core.vulkan.pipeline.VulkanPipelineLayout;
import com.vke.core.rendering.Samplers;
import com.vke.core.vulkan.shr.service.ShaderReflector;
import com.vke.core.vulkan.swapchain.VulkanSwapchain;
import com.vke.core.vulkan.sync.VulkanFence;
import com.vke.core.vulkan.sync.VulkanSemaphore;
import com.vke.core.window.Window;
import com.vke.utils.console.AnsiColors;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.KHRSwapchain;

import java.io.IOException;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

import static com.vke.core.VKEngine.PROFILER;

public class VulkanRenderer extends ServiceImpl implements Renderer, Framable {

    // Vulkan Stuff
    VulkanSwapchain swapchain;
    VulkanRenderDevice device;
    private VulkanFrame[] frames;
    private VulkanFence[] imagesInFlight;
    private VulkanSemaphore[] imagePresentInFlight;

    private VulkanFrame immediateFrame;

    private EngineDescriptorSetsManager engineSetsManager;
    private VulkanResourceManager resourceManager;

    // Engine infos
    final FrameCounter frameCounter;
    private final VKEngine engine;
    private final Context baseContext;
    private final EngineCreateInfo createInfo;

    private VulkanVertexConsumerProvider vertexConsumerProvider;

    private VulkanRenderSystem ctx;
    FrameData frameData;

    private int bindlessTexturesCount;

    public VulkanRenderer(Context context, EngineCreateInfo createInfo) {
        super(Services.VULKAN_RENDERER, context.getEngine());
        this.frameCounter = new FrameCounter(createInfo.vulkanCreateInfo.framesInFlight);
        this.engine = context.getEngine();
        this.baseContext = context;
        this.createInfo = createInfo;
    }

    @Override
    protected void onInitialize() {
        this.ctx = new VulkanRenderSystem(baseContext, this);

        baseContext.getEngine().registerFramable(this);

        this.device = new VulkanRenderDevice(ctx);
        this.bindlessTexturesCount = Math.min(device.capabilities().maxBindlessSampledImages, 8192);
        this.swapchain = device.createSwapchain(new Swapchain.Description(createInfo.vsync, engine.getWindow().getHandle()));
        this.imagesInFlight = new VulkanFence[this.swapchain.getImageCount()];
        this.imagePresentInFlight = new VulkanSemaphore[this.swapchain.getImageCount()];

        this.resourceManager = new VulkanResourceManager(ctx);
        this.vertexConsumerProvider = new VulkanVertexConsumerProvider(ctx);

        for (int i = 0; i < this.swapchain.getImageCount(); i++) {
            imagePresentInFlight[i] = VulkanSemaphore.createSemaphore(ctx);
        }

        // VKE shader to set default descriptors via reflection instead of hard coding
        var temp = R.shaders.get("vke_sets");
        Shader s = null;
        try {
            s = temp.acquire(baseContext);
        } catch (IOException e) {
            baseContext.throwException(new IllegalStateException("Couldnt load shader vke_sets.vsh which is an engine internal shader and has to exist. -> Give up and die"), "VulkanRenderer#onInitialize");
        }
        engineSetsManager = new EngineDescriptorSetsManager(ctx,
                baseContext.<ShaderReflector>service(Services.SHADER_REFLECTION).get(0)
                        .unwrapOrPanic(new IllegalStateException("Failed to find reflected shader for shader ID: 0")));
        s.free();
        engineSetsManager.ENGINE_PIPELINE_LAYOUT = VulkanPipelineLayout.getLayout(ctx, null,
                engineSetsManager.ENGINE_LAYOUTS.entrySet().stream()
                .sorted(Comparator.comparingInt(Map.Entry::getKey))
                .map(Map.Entry::getValue)
                .toList());
        engineSetsManager.makeFrameDataManager();

        this.immediateFrame = device.createImmediateFrame();
        this.frames = device.createFrames();
        RenderPipelines.init(ctx);
    }

    @Override
    public void preFrame() {
        MemoryStack stack = MemoryStack.stackPush();

        VulkanFrame frame = frames[frameCounter.currentIndex()];
        VulkanFence fence = frame.getRenderFence();

        PROFILER.begin("Frame Fence");
        fence.waitForFence();
        PROFILER.end();

        PROFILER.begin("Image Acquire");
        int imageIndex = swapchain.acquireNextImage(frame.getImageSemaphore());
        if (imageIndex < 0 || imageIndex > imagesInFlight.length) {
            int errorCode = ~imageIndex;
            if (errorCode == KHRSwapchain.VK_ERROR_OUT_OF_DATE_KHR) {
                swapchain.recreate();
            }
            stack.close();
            PROFILER.closeStack();
            baseContext.getEngine().skipThisFrame();
            return;
        }
        PROFILER.end();

        PROFILER.begin("Flight Fence");
        //if (imagesInFlight[imageIndex] != null) {
        //    imagesInFlight[imageIndex].waitForFence();
        //}

        fence.reset();

        imagesInFlight[imageIndex] = fence;
        PROFILER.end();

        PROFILER.begin("Cmd Buffers", AnsiColors.BLUE);
        PROFILER.begin("Get");
        VulkanCmdBuffers cmd = frame.getBuffers();
        cmd.reset();
        PROFILER.end();

        PROFILER.begin("Begin");
        cmd.begin();
        Framable framables = baseContext.getEngine().getFramables();
        framables.preRendering();
        cmd.beginRendering();
        PROFILER.end();
        PROFILER.end();

        Window window = baseContext.getEngine().getWindow();
        int width = window.getSize().width();
        int height = window.getSize().height();

        Scissor sc = new Scissor(0, 0, width, height);
        Viewport wp = new Viewport(0, 0, width, height);

        cmd.setViewport(wp);
        cmd.setScissor(sc);

        getEngineSetsManager().onFrame();
        getVertexConsumerProvider().beginFrame();

        this.frameData = new FrameData(frame, cmd, stack, imageIndex);
    }

    @Override
    public void postFrame() {
        VulkanCmdBuffers cmd = frameData.frame().getBuffers();

        try {
            VulkanPipelineLayout.LAYOUT_CACHE.values().forEach(layout -> {
                layout.getSets().forEach(set -> set.bindings.values()
                        .stream().filter(binding -> binding instanceof BufferBinding)
                        .forEach(b -> ((BufferBinding) b).nextFrame()));
                layout.getGroup().getHandleCache().values().stream()
                        .filter(uh -> uh instanceof BufferHandle)
                        .forEach(uh -> ((BufferHandle) uh).nextFrame());
            });
        } catch (ConcurrentModificationException _) {} // I think this happens when asset loader loads a pipeline off thread but whatever

        cmd.endRendering();

        Framable framables = baseContext.getEngine().getFramables();
        framables.postRendering();
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
        immediateSubmit((stack, cmd) -> {
            consumer.accept(stack, cmd);
            return finisher;
        });
    }

    public void immediateSubmit(BiFunction<MemoryStack, VulkanCmdBuffers, Runnable> func) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            VulkanFence fence = immediateFrame.getRenderFence();
            VulkanCmdBuffers cmd = immediateFrame.getBuffers();

            fence.reset();
            cmd.reset();

            cmd.beginImmediate();
            Runnable finisher = func.apply(stack, cmd);
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
    public VulkanRenderSystem renderSystem() {
        return this.ctx;
    }

    @Override
    public VulkanResourceManager resourceManager() {
        return resourceManager;
    }

    @Override
    public VertexConsumerProvider getVertexConsumerProvider() {
        return this.vertexConsumerProvider;
    }

    public EngineCreateInfo getCreateInfo() {
        return createInfo;
    }

    public EngineDescriptorSetsManager getEngineSetsManager() { return this.engineSetsManager; }

    public void scheduleDescriptorUpdate(VulkanPipelineLayout layout, UniformHandle handle) {
        getEngineSetsManager().scheduleDescriptorUpdate(layout, handle, frameCounter);
    }

    public int getBindlessTexturesCount() { return this.bindlessTexturesCount; }

    @Override
    public void free() {
        baseContext.getEngine().removeFramable(this);
        getVertexConsumerProvider().free();
        resourceManager.free();
        // Pipelines get freed by the asset manager
        engineSetsManager.free();
        Arrays.stream(frames).forEach(VulkanFrame::free);
        Arrays.stream(imagePresentInFlight).forEach(VulkanSemaphore::free);
        this.immediateFrame.free();
        this.swapchain.free();
        this.device.free();
    }

    public record FrameData(VulkanFrame frame, VulkanCmdBuffers cmd, MemoryStack stack, int imageIndex) {}

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
