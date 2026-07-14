package com.vke.core.game.camera;

import com.vke.api.game.camera.Camera;
import com.vke.api.game.camera.CameraController;
import com.vke.api.rendering.abstraction.RenderSystem;
import com.vke.api.rendering.abstraction.Renderer;
import com.vke.api.window.Window;
import com.vke.core.Context;
import com.vke.core.services2.Services;
import com.vke.core.window.GlfwWindow;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class PerspectiveCamera implements Camera {

    private final Context context;
    private final RenderSystem renderSystem;

    private Matrix4f projection, view;

    private Vector3f position = new Vector3f();
    private Quaternionf rotation = new Quaternionf();

    private CameraController controller;

    private float FOV, near, far;

    public PerspectiveCamera(Context context, float FOV) {
        this(context, FOV, 0.1f, 1000f);
    }

    public PerspectiveCamera(Context context, float FOV, float near, float far) {
        this.FOV = FOV;
        this.near = near;
        this.far = far;
        this.context = context;
        this.renderSystem = context.<Renderer>service(Services.RENDERER).renderSystem();
        this.remakeViewMatrix();
        onWindowChange(context.getEngine().getWindow());
    }

    @Override
    public void onWindowChange(Window window) {
        Window.Size size = window.getSize();
        this.projection = new Matrix4f()
                .perspective(FOV, (float) size.width() / size.height(), near, far, renderSystem.zZeroToOne());
    }

    @Override
    public Matrix4f viewMatrix() {
        return view;
    }

    @Override
    public Matrix4f projectionMatrix() {
        return projection;
    }

    @Override
    public Vector3f position() {
        return position;
    }

    @Override
    public Quaternionf rotation() {
        return rotation;
    }

    @Override
    public CameraController controller() {
        return this.controller;
    }

    @Override
    public void setPosition(Vector3f position) {
        this.position = position;
        remakeViewMatrix();
    }

    @Override
    public void setRotation(Quaternionf rotation) {
        this.rotation = rotation;
        remakeViewMatrix();
    }

    @Override
    public void setController(CameraController controller) {
        Camera.super.setController(controller);
        this.controller = controller;
    }

    @Override
    public void detachController() {
        Camera.super.detachController();
        this.controller = null;
    }

    @Override
    public void use() {
        this.renderSystem.frameDataManager().setCamera(this);
    }

    public void remakeViewMatrix() {
        this.view = new Matrix4f()
                .rotate(rotation.conjugate(new Quaternionf()))
                .translate(-position.x, -position.y, -position.z);
    }

}
