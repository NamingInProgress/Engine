package com.vke.core.scene.service;

import com.vke.api.scene.LoadingScene;
import com.vke.api.scene.Scene;
import com.vke.api.scene.SceneException;
import com.vke.api.services2.ScopedServiceImpl;
import com.vke.core.Context;
import com.vke.core.FileIdentifier;
import com.vke.core.Identifier;
import com.vke.core.VKEngine;
import com.vke.core.scene.SceneVCL;
import com.vke.core.services2.Services;
import com.vke.utils.Utils;
import com.vke.utils.io.FileUtils;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SceneManagerBaseImpl extends ScopedServiceImpl<SceneManagerScopedImpl> implements SceneManager {
    private final VKEngine engine;
    private HashMap<Identifier, SceneEntry> sceneRegistry;
    private Scene currentlyLoadedScene;
    private Map<Identifier, List<AwaitingDependency>> awaitingDependencies;

    public SceneManagerBaseImpl(VKEngine engine) {
        super(Services.SCENE_MANAGER, engine);
        this.engine = engine;
    }

    @Override
    protected void onInitialize() {
        this.sceneRegistry = new HashMap<>();
        this.awaitingDependencies = new HashMap<>();
    }

    void registerScenes(FileIdentifier sceneDirectory, Context context) {
        sceneDirectory.walkFiles()
                .filter(this::isSceneFile)
                .forEach(sceneFile -> registerScene(sceneFile, context));
    }

    private void registerScene(FileIdentifier sceneFile, Context context) {
        try {
            SceneVCL sceneVCL = new SceneVCL(sceneFile, context);
            Identifier thisName = sceneVCL.name;

            Class<?> clazz = sceneVCL.clazz;
            verifyClassHierarchy(clazz);

            @SuppressWarnings("unchecked")
            Class<? extends Scene> actualSceneClass = (Class<? extends Scene>) clazz;
            Scene instance = createInstance(actualSceneClass, thisName, context);

            instance.setGraph(sceneVCL.renderGraph);

            if (sceneVCL.config != null) {
                instance.acceptConfig(sceneVCL.config);
            }

            Identifier loadingSceneName = sceneVCL.loadingScene;
            if (loadingSceneName == null && !(instance instanceof LoadingScene)) {
                loadingSceneName = new Identifier(VKEngine.VKE_NAMESPACE, "default_loading");
            }

            SceneEntry tried = sceneRegistry.get(loadingSceneName);
            if (tried == null) {
                awaitDependency(loadingSceneName, new AwaitingDependency(instance, loadingSceneName));
            } else {
                instance.setLoadingScene((LoadingScene) tried.scene);
            }

            List<AwaitingDependency> awaitingDependency = awaitingDependencies.get(thisName);
            if (awaitingDependency != null) {
                for (AwaitingDependency waiting : awaitingDependency) {
                    waiting.tryParent(instance);
                }
                awaitingDependencies.remove(thisName);
            }

            sceneRegistry.put(thisName, new SceneEntry(instance, sceneVCL));
        } catch (SceneException e) {
            engine.throwException(e, "Loading scene " + sceneFile);
        }
    }

    private void awaitDependency(Identifier depName, AwaitingDependency waiting) {
        List<AwaitingDependency> list = awaitingDependencies.computeIfAbsent(depName, _ -> new ArrayList<>());
        list.add(waiting);
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
        SceneVCL xml = sceneEntry.sceneVCL();

        if (scene == null) {
            throw new SceneException("Scene " + name + " does not exist! Did you forget to call SceneManager#initialize on the required namespace?");
        }

        try {
            if (currentlyLoadedScene != null) {
                currentlyLoadedScene.onUnload();
                currentlyLoadedScene.free();
            }
        } catch (Exception e) {
            throw new SceneException(e);
        }

        LoadingScene loadingScene = scene.getLoadingScene();
        if (loadingScene == null) throw new SceneException("Cannot find the LoadingScene of " + scene.getName() + ". Maybe your target SceneManager was not initialized yet to load it in?");

        currentlyLoadedScene = loadingScene;
        try {
            loadingScene.loadBundles(xml.bundles, () -> {
                scene.onLoad();
                currentlyLoadedScene = scene;
            });
        } catch (Exception e) {
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
            try {
                currentlyLoadedScene.onUnload();
                currentlyLoadedScene.free();
            } catch (Exception e) {
                engine.throwException(e, "Scene#unload");
            }
        }
    }

    private boolean isSceneFile(FileIdentifier identifier) {
        return "vcl".equals(FileUtils.getExtensionLower(identifier));
    }

    private record SceneEntry(Scene scene, SceneVCL sceneVCL) {}

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
