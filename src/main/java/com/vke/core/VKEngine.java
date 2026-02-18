package com.vke.core;

import com.vke.api.app.App;
import com.vke.api.logger.Logger;
import com.vke.api.registry.VKERegistrate;
import com.vke.api.registry.VKERegistries;
import com.vke.api.services.Service;
import com.vke.api.services.ServiceCreateContext;
import com.vke.core.logger.SOUT;
import com.vke.core.logger.LoggerFactory;
import com.vke.core.services.profiler.DummyProfiler;
import com.vke.core.services.profiler.Profiler;
import com.vke.core.vulkan.VulkanRenderer;
import com.vke.core.vulkan.pipeline.RenderPipelines;
import com.vke.core.services.Services;
import com.vke.core.window.Window;
import com.vke.utils.AnsiColors;
import com.vke.utils.Disposable;
import com.vke.utils.Infallible;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

import java.util.HashSet;
import java.util.Set;

public class VKEngine {
    private final Logger logger;
    private final Logger soutLogger;

    private final Window window;

    private App app;

    public static final VKERegistrate REGISTRATE = VKERegistries.get("vke");
    public static Profiler profiler;

    private final ServiceCreateContext scc;

    private final EngineCreateInfo createInfo;

    private final Set<Service> loadedServices = new HashSet<>();

    private boolean vsync;

    public VKEngine(EngineCreateInfo createInfo) {
        scc = new ServiceCreateContext(this, createInfo);

        Services.init();
        RenderPipelines.init();

        profiler = new DummyProfiler();

        vsync = createInfo.vsync;

        this.createInfo = createInfo;
        logger = LoggerFactory.get(VKEngine.class.getName());
        soutLogger = LoggerFactory.get(SOUT.TAG);
        SOUT.redirect(soutLogger);
        this.window = new Window(this, createInfo.windowCreateInfo);

        GLFW.glfwShowWindow(this.window.getHandle());
    }

    @SuppressWarnings("unchecked")
    public <T extends Service> T service(String key) {
        Service s = VKERegistries.SERVICES.get(key, scc);
        if (s == null) {
            logger.error("Tried to access service \"%s\", but it wasn't registered!", key);
            return null;
        }

        loadedServices.add(s);
        s.getDependencies().forEach(this::service);

        if (key.equals(Services.PROFILER)) profiler = (Profiler) s;

        return (T) s;
    }

    public boolean isServiceLoaded(String key) {
        return loadedServices.stream().anyMatch(service -> service.getId().equals(key));
    }


    public void start(App app) {
        this.app = app;
        app.onInit(this);

        VulkanRenderer renderer = service(Services.VULKAN_RENDERER);
        while (!GLFW.glfwWindowShouldClose(window.getHandle())) {
            if (!window.isMinimized()) {
                profiler.beginFrame();
                profiler.begin("Render", AnsiColors.RED);
                profiler.push();
                profiler.begin("Frame Setup");
                VulkanRenderer.FrameData bfd = renderer.startFrame();
                profiler.end();
                profiler.pop();
                if (bfd != null) {
                    profiler.begin("App Draw", AnsiColors.GREEN);
                    app.onDraw(window, bfd);
                    profiler.end();
                    profiler.begin("Frame End");
                    renderer.endFrame(bfd);
                    profiler.end();
                }
                profiler.end();
                profiler.endFrame();
            }

            GLFW.glfwPollEvents();
        }

        // TODO: Fix me
        this.<VulkanRenderer>service(Services.VULKAN_RENDERER).getDevice().waitIdle();
        free();
    }

    public @NotNull Infallible throwException(Throwable e, String where) {
        logger.fatal("Fatal exception at %s", where);
        throw new RuntimeException(e);
    }

    private void free() {
        app.free();
        window.close();
        loadedServices.forEach(Disposable::free);
    }
    public Window getWindow() {
        return this.window;
    }
    public Logger getLogger() {
        return logger;
    }

    public boolean isDebugMode() { return !this.createInfo.releaseMode; }

}
