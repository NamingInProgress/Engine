package com.vke.api.rendering.abstraction.renderer.data;

import com.vke.api.framable.Framable;
import com.vke.impl.gameobject.CameraGameObject;
import com.vke.utils.io.Disposable;

public interface FrameDataManager extends Framable, Disposable {

    void setCamera(CameraGameObject camera);

}
