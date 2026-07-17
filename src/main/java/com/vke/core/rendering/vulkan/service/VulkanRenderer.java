package com.vke.core.rendering.vulkan.service;

import com.vke.api.framable.Framable;
import com.vke.api.assets.r.R;
import com.vke.api.rendering.FrameCounter;
import com.vke.api.rendering.abstraction.renderer.Renderer;
import com.vke.api.rendering.abstraction.renderer.commands.CommandBuffer;
import com.vke.api.rendering.abstraction.draw.VertexConsumerProvider;
import com.vke.api.rendering.abstraction.renderer.enums.QueueType;
import com.vke.api.rendering.abstraction.renderer.shader.Shader;
import com.vke.api.rendering.abstraction.renderer.swapchain.Swapchain;
import com.vke.api.scene.Scene;
import com.vke.core.rendering.graph.GraphContext;
import com.vke.core.rendering.graph.RenderGraph;
import com.vke.api.rendering.vulkan.descriptors2.handles.UniformHandle;
import com.vke.api.rendering.vulkan.descriptors2.handles.buf.BufferHandle;
import com.vke.api.services2.ServiceImpl;
import com.vke.api.window.Window;
import com.vke.core.Context;
import com.vke.core.EngineCreateInfo;
import com.vke.core.VKEngine;
import com.vke.core.framable.service.FramableManager;
import com.vke.core.rendering.graph.service.GraphManager;
import com.vke.core.rendering.pipeline.RenderPipelines;
import com.vke.core.rendering.vertexconsumer.VulkanVertexConsumerProvider;
import com.vke.core.rendering.vulkan.descriptor.ds2.DescriptorSetInstance;
import com.vke.core.scene.service.SceneManager;
import com.vke.core.services2.Services;
import com.vke.core.rendering.vulkan.Scissor;
import com.vke.core.rendering.vulkan.Viewport;
import com.vke.core.rendering.vulkan.VulkanFrame;
import com.vke.core.rendering.vulkan.command.VulkanCmdBuffers;
import com.vke.core.rendering.vulkan.data.VulkanResourceManager;
import com.vke.core.rendering.vulkan.descriptor.EngineDescriptorSetsManager;
import com.vke.core.rendering.vulkan.device.VulkanRenderDevice;
import com.vke.core.rendering.vulkan.pipeline.VulkanPipelineLayout;
import com.vke.core.rendering.vulkan.shr.service.ShaderReflector;
import com.vke.core.rendering.vulkan.swapchain.VulkanSwapchain;
import com.vke.core.rendering.vulkan.sync.VulkanFence;
import com.vke.core.rendering.vulkan.sync.VulkanSemaphore;
import com.vke.utils.console.AnsiColors;
import com.vke.utils.io.Identifier;
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

    //needed services
    private FramableManager framableManager;
    private SceneManager sceneManager;
    private GraphManager graphManager;

    private VulkanVertexConsumerProvider vertexConsumerProvider;

    private VulkanRenderSystem ctx;
    FrameData frameData;

    private int bindlessTexturesCount;

    public VulkanRenderer(Context context, EngineCreateInfo createInfo) {
        super(Services.RENDERER, context.getEngine());
        this.frameCounter = new FrameCounter(createInfo.vulkanCreateInfo.framesInFlight);
        this.engine = context.getEngine();
        this.baseContext = context;
        this.createInfo = createInfo;
    }

    @Override
    protected void onInitialize() {
        this.ctx = new VulkanRenderSystem(baseContext, this);
        this.framableManager = baseContext.service(Services.FRAMABLE_MANAGER);
        this.sceneManager = baseContext.service(Services.SCENE_MANAGER);
        this.graphManager = baseContext.service(Services.GRAPH_MANAGER);
        graphManager.initialize();

        framableManager.registerFramable(this);

        this.device = new VulkanRenderDevice(ctx);
        int devImgs = device.capabilities().maxBindlessSampledImages;
        int imgs = Math.min(devImgs, 8192);
        this.bindlessTexturesCount = imgs < 0 ? 8192 : imgs;
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
        framableManager.registerFramable(this.getEngineSetsManager().frameDataManager);

        graphManager.onRendererAvailable();
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
            framableManager.skipThisFrame();
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
        Framable framables = framableManager.getAllFramables();
        framables.preRendering();
        //cmd.beginRendering();
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
        Scene currentScene = sceneManager.getCurrentScene();
        Identifier graphId = currentScene.getGraph();
        RenderGraph graph = graphManager.getGraph(graphId);
        currentScene.onPrepareRendering(graph.getContext());
    }

    @Override
    public void onDraw() {
        Scene currentScene = sceneManager.getCurrentScene();
        Identifier graphId = currentScene.getGraph();
        RenderGraph graph = graphManager.getGraph(graphId);
        graph.onDraw(currentScene);
    }

    @Override
    public void postFrame() {
        VulkanCmdBuffers cmd = frameData.frame().getBuffers();

        try {
            VulkanPipelineLayout.LAYOUT_CACHE.values().forEach(layout -> {
//                layout.getSets().forEach(set -> set.bindings.values()
//                        .stream().filter(binding -> binding instanceof BufferBinding)
//                        .forEach(b -> ((BufferBinding) b).nextFrame()));
                layout.getSets().forEach(DescriptorSetInstance::onNewFrame);
                layout.getGroup().getHandleCache().values().stream()
                        .filter(uh -> uh instanceof BufferHandle)
                        .forEach(uh -> ((BufferHandle) uh).nextFrame());
            });
        } catch (ConcurrentModificationException _) {} // I think this happens when asset loader loads a pipeline off thread but whatever

        //cmd.endRendering();

        Framable framables = framableManager.getAllFramables();
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
        return List.of(Services.SHADER_COMPILER, Services.ASSET_MANAGER, Services.SCENE_MANAGER, Services.GRAPH_MANAGER);
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

    @Override
    public void beforeTerminate() {
        getDevice().waitIdle();
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
        framableManager.removeFramable(this);
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
