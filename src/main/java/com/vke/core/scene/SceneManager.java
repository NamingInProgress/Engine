package com.vke.core.scene;

import com.vke.api.parsing.config.ConfigDocument;
import com.vke.api.registry.registries.VKERegistry;
import com.vke.api.scene.Scene;
import com.vke.api.services.Service;
import com.vke.core.VKEngine;
import com.vke.core.services.Services;
import com.vke.api.app.Namespace;
import com.vke.utils.io.Identifier;

import java.io.IOException;
import java.util.List;

public class SceneManager extends Service {
    private final VKEngine engine;
    private final VKERegistry.ID<Scene> sceneRegistry;

    public SceneManager(VKEngine engine) {
        super(Services.SCENE_MANAGER);
        this.engine = engine;
        this.sceneRegistry = new VKERegistry.ID<>(engine.id("scenes"));

        for (Namespace ns : engine.getAllNamespaces()) {
            registerScenes(ns.id("scenes"));
        }
    }

    private void registerScenes(Identifier sceneDirectory) {
        sceneDirectory.walkFiles().forEach(this::registerScene);
    }

    private void registerScene(Identifier sceneXML) {
        try {
            ConfigDocument document = ConfigDocument.parseIdentifier(sceneXML);

        } catch (IOException e) {
            engine.throwException(e, "Loading scene " + sceneXML);
        }
    }

    public void setScene(Identifier name) {

    }

    @Override
    protected List<String> dependencies() {
        return List.of(Services.ASSET_MANAGER);
    }

    @Override
    public void free() {

    }
}
