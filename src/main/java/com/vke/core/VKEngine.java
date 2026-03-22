package com.vke.core;

import com.vke.api.app.App;
import com.vke.api.app.Version;
import com.vke.api.event.EventBus;
import com.vke.api.logger.Logger;
import com.vke.api.registry.VKERegistrate;
import com.vke.api.registry.VKERegistries;
import com.vke.api.services.Service;
import com.vke.api.services.ServiceCreateContext;
import com.vke.core.event.events.lifetime.AppLifecycleEvents;
import com.vke.core.logger.SOUT;
import com.vke.core.logger.LoggerFactory;
import com.vke.core.services.ServiceManager;
import com.vke.core.profiler.DummyProfiler;
import com.vke.core.profiler.Profiler;
import com.vke.core.vulkan.VulkanRenderer;
import com.vke.core.services.Services;
import com.vke.core.window.Window;
import com.vke.utils.console.AnsiColors;
import com.vke.utils.io.Identifier;
import com.vke.api.app.Namespace;
import com.vke.utils.iter.Iter;
import org.lwjgl.glfw.GLFW;

import java.util.*;

public class VKEngine extends Context {
    public static final String VKE_NAMESPACE = "vke";
    public static final VKERegistrate REGISTRATE = VKERegistries.get(VKE_NAMESPACE);

    public static Profiler profiler;

    private final Logger soutLogger;

    private final EngineCreateInfo createInfo;
    private final ServiceManager serviceManager;

    private Window window;
    private App app;
    public EventBus EVENT_BUS;

    private final List<String> namespaces;

    public VKEngine(EngineCreateInfo createInfo) {
        super(Namespace.of(VKE_NAMESPACE));

        if (!createInfo.releaseMode) System.out.println("Process Handle: " + ProcessHandle.current().pid());

        this.createInfo = createInfo;
        this.namespaces = new ArrayList<>() {{ add(VKE_NAMESPACE);
            if (!getAppNamespace().equals(VKE_NAMESPACE)) add(VKE_NAMESPACE);
        }};
        this.soutLogger = LoggerFactory.get(SOUT.TAG);

        ServiceCreateContext scc = new ServiceCreateContext(this, createInfo);
        this.serviceManager = new ServiceManager(scc);

        SOUT.redirect(soutLogger);

        profiler = new DummyProfiler();
        EVENT_BUS = service(Services.EVENT_BUS);
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

    public App getApp() {
        return app;
    }

    public Version getAppVersion() {
        return createInfo.applicationVersion;
    }

    public Identifier idForLocale(Locale locale) {
        return id(locale.getLanguage());
    }
    public String getAppNamespace() {
        return createInfo.applicationNamespace;
    }

    public boolean isDebugMode() { return !this.createInfo.releaseMode; }

    public Iter<String> getAllNamespacesStr() {
        return Iter.of(namespaces);
    }

    public Iter<Namespace> getAllNamespaces() {
        return getAllNamespacesStr().map(Namespace::of);
    }

    public EngineCreateInfo.RendererType rendererType() {
        return createInfo.rendererType;
    }

    public ServiceManager getServiceManager() { return this.serviceManager; }

    @Override
    public VKEngine getEngine() {
        return this;
    }
}
