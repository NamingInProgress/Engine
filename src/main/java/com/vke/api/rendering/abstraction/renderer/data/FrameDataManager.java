package com.vke.api.rendering.abstraction.renderer.data;

import com.vke.api.framable.Framable;
import com.vke.api.game.camera.Camera;
import com.vke.core.game.object.CameraGameObject;

public interface FrameDataManager extends Framable {

    void setCamera(CameraGameObject camera);

}
