package com.vke.core;

import com.vke.api.app.App;
import com.vke.api.app.Version;
import com.vke.api.event.EventBus;
import com.vke.api.logger.Logger;
import com.vke.api.registry.VKERegistrate;
import com.vke.api.registry.VKERegistries;
import com.vke.api.services.Service;
import com.vke.api.services.ServiceCreateContext;
import com.vke.core.event.events.ServiceLoadEvent;
import com.vke.core.event.events.lifetime.AppLifecycleEvents;
import com.vke.core.logger.SOUT;
import com.vke.core.logger.LoggerFactory;
import com.vke.core.services.ServiceManager;
import com.vke.core.services.profiler.DummyProfiler;
import com.vke.core.services.profiler.Profiler;
import com.vke.core.vulkan.VulkanRenderer;
import com.vke.core.services.Services;
import com.vke.core.window.Window;
import com.vke.utils.AnsiColors;
import com.vke.utils.Disposable;
import com.vke.utils.Identifier;
import com.vke.utils.Infallible;
import com.vke.utils.iter.Iter;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

import java.util.*;

public class VKEngine {

    public static final String VKE_NAMESPACE = "vke";
    public static final VKERegistrate REGISTRATE = VKERegistries.get(VKE_NAMESPACE);

    public static Profiler profiler;

    private final Logger logger;
    private final Logger soutLogger;

    private final ServiceCreateContext scc;
    private final EngineCreateInfo createInfo;
    private final ServiceManager serviceManager;

    private Window window;
    private App app;
    public EventBus EVENT_BUS;

    private final List<String> namespaces;

    public VKEngine(EngineCreateInfo createInfo) {
        if (!createInfo.releaseMode) System.out.println("Process Handle: " + ProcessHandle.current().pid());

        this.createInfo = createInfo;
        this.namespaces = new ArrayList<>() {{ add(VKE_NAMESPACE);
            if (!getAppNamespace().equals(VKE_NAMESPACE)) add(VKE_NAMESPACE);
        }};
        this.logger = LoggerFactory.get(VKEngine.class.getName());
        this.soutLogger = LoggerFactory.get(SOUT.TAG);
        this.scc = new ServiceCreateContext(this, createInfo);
        this.serviceManager = new ServiceManager(scc);

        SOUT.redirect(soutLogger);

        profiler = new DummyProfiler();
        EVENT_BUS = service(Services.EVENT_BUS);
    }

    public <T extends Service> T service(String key) {
        return this.serviceManager.service(key);
    }

    public void start(App app) {
        this.window = new Window(this, createInfo.windowCreateInfo);

        this.app = app;
        AppLifecycleEvents.PreLoad event = new AppLifecycleEvents.PreLoad(app);
        EVENT_BUS.fire(event);
        this.namespaces.addAll(event.getPlugins());

        app.onInit(this);
        EVENT_BUS.fire(new AppLifecycleEvents.PostLoad(app));

        VulkanRenderer renderer = service(Services.VULKAN_RENDERER);

        this.window.show();
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
        EVENT_BUS.fire(new AppLifecycleEvents.PreFree(app));
        app.free();
        EVENT_BUS.fire(new AppLifecycleEvents.PostFree(app));

        serviceManager.free();
        window.close();
    }

    public Window getWindow() {
        return this.window;
    }
    public Logger getLogger() {
        return logger;
    }
    public App getApp() {
        return app;
    }
    public Version getAppVersion() {
        return createInfo.applicationVersion;
    }
    public Identifier id(String pathOrIdent) {
        if (pathOrIdent.indexOf(':') >= 0) {
            return Identifier.of(pathOrIdent);
        }
        return new Identifier(createInfo.applicationNamespace, pathOrIdent);
    }
    public Identifier idForLocale(Locale locale) {
        return id(locale.getLanguage());
    }
    public String getAppNamespace() {
        return createInfo.applicationNamespace;
    }

    public boolean isDebugMode() { return !this.createInfo.releaseMode; }

    public Iter<String> getAllNamespaces() {
        return Iter.of(namespaces);
    }

    public EngineCreateInfo.RendererType rendererType() {
        return createInfo.rendererType;
    }

    public ServiceManager getServiceManager() { return this.serviceManager; }
}
