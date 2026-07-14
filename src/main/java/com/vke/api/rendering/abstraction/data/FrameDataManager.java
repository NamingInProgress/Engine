package com.vke.api.rendering.abstraction.data;

import com.vke.api.app.Framable;
import com.vke.api.game.camera.Camera;

public interface FrameDataManager extends Framable {

    void setCamera(Camera camera);

}
