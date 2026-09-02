package com.vke.core.game.object;

import com.vke.api.event.EventBus;
import com.vke.api.event.EventListener;
import com.vke.api.event.SubscribeEvent;
import com.vke.api.rendering.abstraction.renderer.RenderSystem;
import com.vke.api.rendering.abstraction.renderer.Renderer;
import com.vke.api.window.Window;
import com.vke.api.window.WindowResizeEvent;
import com.vke.core.Context;
import com.vke.core.ecs.ComponentReference;
import com.vke.core.ecs.component.mask.ComponentMask;
import com.vke.core.services2.Services;
import com.vke.impl.ecs.TransformC;
import com.vke.impl.ecs.camera.CameraC;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class CameraGameObject extends AbstractGameObject implements RestrictedGameObject, EventListener {
    private static final Vector3f NEG_Z = new Vector3f(0, 0, -1);
    private static final int[] CONST_IDS = new int[] { TransformC.ID, CameraC.ID };

    private ComponentReference<CameraC> cameraComponentRef;
    private Matrix4f projMatrix;

    public CameraGameObject(Context ctx) {
        super(ctx);

        EventBus eventBus = ctx.service(Services.EVENT_BUS);
        eventBus.register(this);
    }

    @Override
    protected ComponentMask createMask() {
        return new ComponentMask(TransformC.ID, CameraC.ID);
    }

    @Override
    protected void onSpawned() {
        this.cameraComponentRef = ecs.obtainComponentReference(entityId, CameraC.ID);
        Window window = ctx.getEngine().getWindow();
        Window.Size size = window.getSize();
        onWindowResize(new WindowResizeEvent(window, size.width(), size.height()));
    }

    @SubscribeEvent
    public void onWindowResize(WindowResizeEvent event) {
        //PLEASE BRO I hate these one-liners with the generic
        RenderSystem sys = ctx.<Renderer>service(Services.RENDERER).renderSystem();
        CameraC c = cameraComponentRef.getComponent();
        int idx = cameraComponentRef.getIndex();
        if (c.isOrtho[idx]) {
            projMatrix = new Matrix4f()
                    .perspective(c.fov[idx], (float) event.width / event.height, c.nearPlane[idx], c.farPlane[idx], sys.zZeroToOne());
        } else {
            float halfWidth = event.width / 2.0f / c.zoom[idx];
            float halfHeight = event.height / 2.0f / c.zoom[idx];

            projMatrix = new Matrix4f()
                    .ortho(-halfWidth, halfWidth, -halfHeight, halfHeight, c.nearPlane[idx], c.farPlane[idx], sys.zZeroToOne());
        }
    }

    @Override
    protected GameObject createFromSpawnedEntity(int entity) {
        CameraGameObject n = new CameraGameObject(ctx);
        n.entityId = entity;
        n.projMatrix = new Matrix4f(projMatrix);
        return n;
    }

    @Override
    public int[] getFixedComponents() {
        return CONST_IDS;
    }

    @Override
    public GameObject duplicate() {
        return null;
    }

    public void lookAt(Vector3f lookAt) {
        float x = getRQX();
        float y = getRQY();
        float z = getRQZ();
        float w = getRQW();

        //im trusting gemini on ts
        lookAt.x = -2.0f * (x * z + w * y);
        lookAt.y = -2.0f * (y * z - w * x);
        lookAt.z = 2.0f * (x * x + y * y) - 1.0f;
    }

    public Matrix4f getProjectionMatrix() {
        return projMatrix;
    }

    public void getViewMatrix(Matrix4f viewMatrix) {
        viewMatrix.translationRotate(-getX(), -getY(), -getZ(), getRQX(), getRQY(), getRQZ(), getRQW());
    }
}
