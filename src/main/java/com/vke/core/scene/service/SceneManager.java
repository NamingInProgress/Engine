package com.vke.core.scene.service;

import com.vke.api.scene.Scene;
import com.vke.api.scene.SceneException;
import com.vke.api.services2.Service;
import com.vke.api.services2.StatefulService;
import com.vke.utils.io.Identifier;

public interface SceneManager extends StatefulService<SceneTransferState> {
    void initialize();
    Scene getCurrentScene();
    void setScene(Identifier name) throws SceneException;
    void setScene(String name) throws SceneException;
}
