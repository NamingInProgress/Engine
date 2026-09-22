package com.vke.core;

import com.vke.api.app.App;
import com.vke.api.app.Version;
import com.vke.api.rendering.abstraction.renderer.Renderer;
import com.vke.api.window.Window;
import com.vke.core.assets.CacheHandler;
import com.vke.core.framable.service.FramableManager;
import com.vke.core.mesh.MeshPrefab;
import com.vke.api.event.EventBus;
import com.vke.api.logger.Logger;
import com.vke.api.registry.VKERegistrate;
import com.vke.api.registry.VKERegistries;
import com.vke.core.event.events.lifetime.AppLifecycleEvents;
import com.vke.core.logger.SOUT;
import com.vke.core.logger.LoggerFactory;
import com.vke.core.profiler.service.Profiler;
import com.vke.core.services2.ServiceManager;
import com.vke.core.profiler.DummyProfilerImpl;
import com.vke.core.services2.Services;
import com.vke.core.window.service.WindowManager;
import com.vke.api.app.Namespace;
import com.vke.utils.iter.Iter;

import java.util.*;

public class VKEngine extends Context {
    public static final String VKE_NAMESPACE = "vke";
    public static final VKERegistrate REGISTRATE = VKERegistries.get(VKE_NAMESPACE);

    public static Profiler PROFILER;

    private final Logger soutLogger;

    private final EngineCreateInfo createInfo;
    private final ServiceManager serviceManager;

    private App app;
    public EventBus EVENT_BUS;

    private final FramableManager framableManager;
    private final WindowManager windowManager;
    private Renderer renderer;
    private Window window;

    private final List<String> namespaces;

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

        PROFILER = new DummyProfilerImpl();
        EVENT_BUS = service(Services.EVENT_BUS);

        framableManager = service(Services.FRAMABLE_MANAGER);
        windowManager = service(Services.WINDOW_MANAGER);

        registerSerializers();
    }

    public void start(App app) {
        framableManager.registerFramable(app);

        this.window = windowManager.createWindow(createInfo.windowCreateInfo);

        this.app = app;
        AppLifecycleEvents.PreLoad event = new AppLifecycleEvents.PreLoad(app);
        EVENT_BUS.fire(event);
        this.namespaces.addAll(event.getPlugins());

        app.onInit(this);
        EVENT_BUS.fire(new AppLifecycleEvents.PostLoad(app));

        //needs some basic intitialization done like window and assets
        renderer = service(Services.RENDERER);

        this.window.show();

        renderer.beforeTerminate();
        free();
    }

    private void free() {
        EVENT_BUS.fire(new AppLifecycleEvents.PreFree(app));
        app.free();
        EVENT_BUS.fire(new AppLifecycleEvents.PostFree(app));

        serviceManager.free();
        window.requestClose();
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
        Identifier.registerSerializers();
        FileIdentifier.registerSerializers();
        CacheHandler.registerSerializers();
    }

    public EngineCreateInfo getCreateInfo() {
        return createInfo;
    }

    public void explode() {
        throwException(new RuntimeException("boom!"), "java code");
    }
}
