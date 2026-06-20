package com.vke.core.scene.service;

import com.vke.api.scene.Scene;
import com.vke.utils.io.Identifier;

import java.util.Map;

public record SceneTransferState(Scene currentLoadedScene, Map<Identifier, Entry> sceneRegistry) {
    public record Entry(Scene scene, Identifier file) {
    }
}
