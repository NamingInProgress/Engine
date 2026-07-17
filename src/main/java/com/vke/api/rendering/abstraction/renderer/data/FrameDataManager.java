package com.vke.api.rendering.abstraction.renderer.data;

import com.vke.api.framable.Framable;
import com.vke.api.game.camera.Camera;

public interface FrameDataManager extends Framable {

    void setCamera(Camera camera);

}
