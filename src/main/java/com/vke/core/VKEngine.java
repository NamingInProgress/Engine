package com.vke.core;

import com.vke.api.app.App;
import com.vke.api.app.Framable;
import com.vke.api.app.Version;
import com.vke.api.rendering.abstraction.Renderer;
import com.vke.core.mesh.MeshPrefab;
import com.vke.api.event.EventBus;
import com.vke.api.logger.Logger;
import com.vke.api.registry.VKERegistrate;
import com.vke.api.registry.VKERegistries;
import com.vke.core.event.events.lifetime.AppLifecycleEvents;
import com.vke.core.logger.SOUT;
import com.vke.core.logger.LoggerFactory;
import com.vke.core.services2.ServiceManager;
import com.vke.core.profiler.DummyProfilerImpl;
import com.vke.core.profiler.service.ProfilerImpl;
import com.vke.core.vulkan.service.VulkanRenderer;
import com.vke.core.services2.Services;
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

    public static ProfilerImpl profiler;

    private final Logger soutLogger;

    private final EngineCreateInfo createInfo;
    private final ServiceManager serviceManager;

    private Window window;
    private App app;
    public EventBus EVENT_BUS;

    private final List<String> namespaces;

    private final Framable.Glossary framables;

    public VKEngine(EngineCreateInfo createInfo) {
        super(Namespace.of(VKE_NAMESPACE));


        if (!createInfo.releaseMode) System.out.println("Process Handle: " + ProcessHandle.current().pid());

        this.createInfo = createInfo;
        this.namespaces = new ArrayList<>() {{ add(VKE_NAMESPACE);
            if (!getAppNamespace().equals(VKE_NAMESPACE)) add(VKE_NAMESPACE);
        }};
        this.soutLogger = LoggerFactory.get(SOUT.TAG);

        this.serviceManager = new ServiceManager(this);
        Services.init(serviceManager, this);

        SOUT.redirect(soutLogger);

        profiler = new DummyProfilerImpl();
        EVENT_BUS = service(Services.EVENT_BUS);

        this.framables = new Framable.Glossary();

        registerSerializers();
    }

    public void start(App app) {
        this.framables.addEntry(app);
        this.window = new Window(this, createInfo.windowCreateInfo);

        this.app = app;
        AppLifecycleEvents.PreLoad event = new AppLifecycleEvents.PreLoad(app);
        EVENT_BUS.fire(event);
        this.namespaces.addAll(event.getPlugins());

        app.onInit(this);
        EVENT_BUS.fire(new AppLifecycleEvents.PostLoad(app));

        VulkanRenderer renderer = service(Services.VULKAN_RENDERER).assumeImplementation();

        int reqFps = createInfo.fps;
        long targetFrameTimeNs = reqFps > 0 ? 1_000_000_000L / reqFps : 0L;
        long lastFrameTime = System.nanoTime();

        this.window.show();
        while (!GLFW.glfwWindowShouldClose(window.getHandle())) {
            if (!window.isMinimized()) {
                long now = System.nanoTime();

                if (reqFps != -1 && now - lastFrameTime < targetFrameTimeNs) continue;

                lastFrameTime = now;

                profiler.beginFrame();
                profiler.begin("Render", AnsiColors.RED);
                profiler.push();
                profiler.begin("Frame Setup");
                framables.preFrame();
                VulkanRenderer.FrameData bfd = renderer.startFrame(window, framables);
                profiler.end();
                profiler.pop();
                if (bfd != null) {
                    profiler.begin("App Draw", AnsiColors.GREEN);
                    framables.onDraw(bfd.context());
                    profiler.end();
                    profiler.begin("Frame End");
                    renderer.endFrame(bfd, framables);
                    framables.postFrame();
                    profiler.end();
                }
                profiler.end();
                profiler.endFrame();
            }

            GLFW.glfwPollEvents();
        }

        // TODO: Fix me
        ((Renderer) this.service(Services.VULKAN_RENDERER)).getDevice().waitIdle();
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

    public Context createNewContext(String namespace) {
        return new ModuleContext(Namespace.of(namespace), this);
    }

    private void registerSerializers() {
        MeshPrefab.registerSerializers();
    }

    public void registerFramable(Framable f) {
        this.framables.addEntry(f);
    }

    public void removeFramable(Framable f) {
        this.framables.removeEntry(f);
    }

    public EngineCreateInfo getCreateInfo() {
        return createInfo;
    }

    public void explode() {
        throwException(new RuntimeException("boom!"), "java code");
    }
}
