package com.vke.api.game.camera;

import com.vke.utils.io.Disposable;

public interface CameraController {
    void attachCamera(Camera camera);
    void detachCamera();
}
