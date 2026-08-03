package com.vke.core.game.camera;

import com.vke.api.game.camera.Camera;
import com.vke.api.game.camera.CameraController;
import com.vke.api.rendering.abstraction.renderer.RenderSystem;
import com.vke.api.rendering.abstraction.renderer.Renderer;
import com.vke.api.window.Window;
import com.vke.core.Context;
import com.vke.core.services2.Services;
import com.vke.core.window.callbacks.FramebufferCallbacks;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class OriginOrthoCamera implements Camera {

    private final RenderSystem renderSystem;

    private Matrix4f projection, view;

    private Vector3f position = new Vector3f();
    private Quaternionf rotation = new Quaternionf();

    private CameraController controller;

    private float near, far;
    private float zoom;

    public OriginOrthoCamera(Context context) {
        this(context, 0f, 1000f, 1);
    }

    /**
     * This camera is centered at the bottom left of the window! Keep this in mind.
     */
    public OriginOrthoCamera(Context context, float near, float far, float zoom) {
        this.near = near;
        this.far = far;
        this.zoom = zoom;
        this.renderSystem = context.<Renderer>service(Services.RENDERER).renderSystem();
        this.remakeViewMatrix();
        onWindowChange(context.getEngine().getWindow());
        FramebufferCallbacks.resize((w, h) -> onWindowChange(context.getEngine().getWindow()));
    }

    @Override
    public void setZoom(float zoom) {
        this.zoom = zoom;
        if (this.zoom < 0.1) this.zoom = 0.1f;
        onWindowChange(renderSystem.getEngine().getWindow());
    }

    @Override
    public float zoom() {
        return this.zoom;
    }

    @Override
    public void onWindowChange(Window window) {
        Window.Size size = window.getSize();

        float halfWidth = size.width() / 2.0f / zoom;
        float halfHeight = size.height() / 2.0f / zoom;

        this.projection = new Matrix4f()
                .ortho(-halfWidth, halfWidth, -halfHeight, halfHeight, near, far, renderSystem.zZeroToOne());
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
