package com.vke.core.scene.manager;

import com.vke.api.scene.Scene;
import com.vke.api.scene.SceneException;
import com.vke.api.services.ScopedService;
import com.vke.core.Context;
import com.vke.core.VKEngine;
import com.vke.core.scene.SceneXML;
import com.vke.core.services.Services;
import com.vke.utils.Utils;
import com.vke.utils.io.Identifier;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.List;

public class SceneManagerService extends ScopedService<SceneManager> {
    private final VKEngine engine;
    private final HashMap<Identifier, Scene> sceneRegistry;
    private Scene currentlyLoadedScene;

    public SceneManagerService(VKEngine engine) {
        super(Services.SCENE_MANAGER);
        this.engine = engine;
        this.sceneRegistry = new HashMap<>();
    }

    void registerScenes(Identifier sceneDirectory, Context context) {
        sceneDirectory.walkFiles()
                .filter(this::isSceneFile)
                .forEach(sceneFile -> registerScene(sceneFile, context));
    }

    private void registerScene(Identifier sceneFile, Context context) {
        try {
            SceneXML sceneXML = new SceneXML(sceneFile);
            Identifier thisName = sceneXML.name;

            Class<?> clazz = sceneXML.clazz;
            verifyClassHierarchy(clazz);

            @SuppressWarnings("unchecked")
            Class<? extends Scene> actualSceneClass = (Class<? extends Scene>) clazz;
            Scene instance = createInstance(actualSceneClass, thisName, context);
            sceneRegistry.put(thisName, instance);
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

    public void setScene(Identifier name) throws SceneException {
        Scene scene = sceneRegistry.get(name);
        if (scene == null) {
            throw new SceneException("Scene " + name + " does not exist! Did you forget to call SceneManager#initialize on the required namespace?");
        }

        if (currentlyLoadedScene != null) {
            currentlyLoadedScene.onUnload();
        }
        scene.onLoad();
    }

    public Scene getCurrentScene() {
        return currentlyLoadedScene;
    }

    @Override
    protected List<String> dependencies() {
        return List.of(Services.ASSET_MANAGER);
    }

    @Override
    protected SceneManager createScoped(Context context) {
        return new SceneManager(context, this);
    }

    @Override
    public void free() {

    }

    private boolean isSceneFile(Identifier identifier) {
        return "xml".equals(identifier.getExtensionLower());
    }
}
