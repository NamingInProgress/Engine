package com.vke.core.scene.service;

import com.vke.api.scene.LoadingScene;
import com.vke.api.scene.Scene;
import com.vke.api.scene.SceneException;
import com.vke.api.services2.ScopedServiceImpl;
import com.vke.core.Context;
import com.vke.core.VKEngine;
import com.vke.core.assets.AssetException;
import com.vke.core.scene.SceneXML;
import com.vke.core.services2.Services;
import com.vke.utils.Utils;
import com.vke.utils.io.Identifier;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SceneManagerBaseImpl extends ScopedServiceImpl<SceneManagerScopedImpl> implements SceneManager {
    private final VKEngine engine;
    private HashMap<Identifier, SceneEntry> sceneRegistry;
    private Scene currentlyLoadedScene;
    private Map<Identifier, AwaitingDependency> awaitingDependencies;

    public SceneManagerBaseImpl(VKEngine engine) {
        super(Services.SCENE_MANAGER, engine);
        this.engine = engine;
    }

    @Override
    protected void onInitialize() {
        this.sceneRegistry = new HashMap<>();
        this.awaitingDependencies = new HashMap<>();
    }

    void registerScenes(Identifier sceneDirectory, Context context) {
        sceneDirectory.walkFiles()
                .filter(this::isSceneFile)
                .forEach(sceneFile -> registerScene(sceneFile, context));
    }

    private void registerScene(Identifier sceneFile, Context context) {
        try {
            SceneXML sceneXML = new SceneXML(sceneFile, context);
            Identifier thisName = sceneXML.name;

            Class<?> clazz = sceneXML.clazz;
            verifyClassHierarchy(clazz);

            @SuppressWarnings("unchecked")
            Class<? extends Scene> actualSceneClass = (Class<? extends Scene>) clazz;
            Scene instance = createInstance(actualSceneClass, thisName, context);

            Identifier loadingSceneName = sceneXML.loadingScene;
            if (loadingSceneName == null && !(instance instanceof LoadingScene)) {
                loadingSceneName = new Identifier(VKEngine.VKE_NAMESPACE, "default_loading");
            }

            SceneEntry tried = sceneRegistry.get(loadingSceneName);
            if (tried == null) {
                awaitingDependencies.put(loadingSceneName, new AwaitingDependency(instance, loadingSceneName));
            } else {
                instance.setLoadingScene((LoadingScene) tried.scene);
            }

            AwaitingDependency awaitingDependency = awaitingDependencies.get(thisName);
            if (awaitingDependency != null && awaitingDependency.tryParent(instance)) {
                awaitingDependencies.remove(thisName);
            }

            sceneRegistry.put(thisName, new SceneEntry(instance, sceneXML));
        } catch (SceneException e) {
            engine.throwException(e, "Loading scene " + sceneFile);
        }
    }

    private void verifyClassHierarchy(Class<?> sceneClass) throws SceneException {
        if (!Scene.class.isAssignableFrom(sceneClass)) {
            throw new SceneException("Class " + sceneClass + " doesnt implement Scene!");
        }
    }

    private Scene createInstance(Class<? extends Scene> clazz, Identifier name, Context context) throws SceneException {
        return Utils.chainExceptions(() -> {
            Constructor<? extends Scene> constructor = clazz.getDeclaredConstructor(Identifier.class, Context.class);
            return constructor.newInstance(name, context);
        });
    }

    @Override
    public void setScene(Identifier name) throws SceneException {
        SceneEntry sceneEntry = sceneRegistry.get(name);
        Scene scene = sceneEntry.scene();
        SceneXML xml = sceneEntry.sceneXML();

        if (scene == null) {
            throw new SceneException("Scene " + name + " does not exist! Did you forget to call SceneManager#initialize on the required namespace?");
        }

        if (currentlyLoadedScene != null) {
            currentlyLoadedScene.onUnload();
            currentlyLoadedScene.free();
        }

        LoadingScene loadingScene = scene.getLoadingScene();
        if (loadingScene == null) throw new SceneException("Cannot find the LoadingScene of " + scene.getName() + ". Maybe your target SceneManager was not initialized yet to load it in?");

        try {
            loadingScene.loadBundles(xml.bundles, () -> {
                scene.onLoad();
                currentlyLoadedScene = scene;
            });
        } catch (AssetException e) {
            throw new SceneException(e);
        }
    }

    @Override
    public void setScene(String name) throws SceneException {
        setScene(engine.id(name));
    }

    @Override
    public Scene getCurrentScene() {
        return currentlyLoadedScene;
    }

    @Override
    public List<String> dependencies() {
        return List.of(Services.ASSET_MANAGER);
    }

    @Override
    protected SceneManagerScopedImpl createScoped(Context context) {
        return new SceneManagerScopedImpl(context, this);
    }

    @Override
    public void free() {
        if (currentlyLoadedScene != null) {
            currentlyLoadedScene.onUnload();
            currentlyLoadedScene.free();
        }
    }

    private boolean isSceneFile(Identifier identifier) {
        return "xml".equals(identifier.getExtensionLower());
    }

    @Override
    public @Nullable SceneTransferState createTransferState() {
        HashMap<Identifier, SceneTransferState.Entry> registry = new HashMap<>(sceneRegistry.size());
        sceneRegistry.forEach((k, v) -> registry.put(k, new SceneTransferState.Entry(v.scene, v.sceneXML.file)));
        return new SceneTransferState(currentlyLoadedScene, registry);
    }

    @Override
    public void applyTransferState(@Nullable SceneTransferState state) {
        applyTransferState(state, engine);
    }

    void applyTransferState(@Nullable SceneTransferState state, Context context) {
        if (state == null) return;

        this.currentlyLoadedScene = state.currentLoadedScene();
        this.sceneRegistry.clear();
        for (SceneTransferState.Entry entry : state.sceneRegistry().values()) {
            registerScene(entry.file(), engine);
        }
    }

    private record SceneEntry(Scene scene, SceneXML sceneXML) {}

    private record AwaitingDependency(Scene waiting, Identifier targetName) {
        public boolean tryParent(Scene parent) {
            if (parent.getName().equals(targetName)) {
                waiting.setLoadingScene((LoadingScene) parent);
                return true;
            }
            return false;
        }
    }
}
