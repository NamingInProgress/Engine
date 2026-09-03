package com.vke.api.rendering.abstraction.renderer.data;

import com.vke.api.framable.Framable;
import com.vke.impl.gameobject.CameraGameObject;

public interface FrameDataManager extends Framable {

    void setCamera(CameraGameObject camera);

}
