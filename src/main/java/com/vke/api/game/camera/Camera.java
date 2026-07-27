package com.vke.api.game.camera;

import com.vke.api.window.Window;
import com.vke.core.window.GlfwWindow;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public interface Camera {
    // TODO: replace with event stuff
    void onWindowChange(Window window);
    Matrix4f viewMatrix();
    Matrix4f projectionMatrix();

    Vector3f position();
    Quaternionf rotation();

    default Vector3f lookAt() {
        Vector3f forward = new Vector3f(0, 0, -1);
        rotation().transform(forward);
        return forward;
    }

    CameraController controller();

    void setPosition(Vector3f position);
    void setRotation(Quaternionf rotation);

    default void setController(CameraController controller) {
        controller.attachCamera(this);
    }

    default void detachController() {
        controller().detachCamera();
    }

    void use();

}
