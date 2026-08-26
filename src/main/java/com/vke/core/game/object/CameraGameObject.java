package com.vke.core.game.object;

import com.vke.api.event.EventBus;
import com.vke.api.event.EventListener;
import com.vke.api.event.SubscribeEvent;
import com.vke.api.rendering.abstraction.renderer.RenderSystem;
import com.vke.api.rendering.abstraction.renderer.Renderer;
import com.vke.api.window.WindowResizeEvent;
import com.vke.core.Context;
import com.vke.core.ecs.ComponentReference;
import com.vke.core.rendering.SmartMatrixUtils;
import com.vke.core.services2.Services;
import com.vke.impl.ecs.TransformC;
import com.vke.impl.ecs.camera.CameraC;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class CameraGameObject extends AbstractGameObject implements RestrictedGameObject, TransformedGameObject, EventListener {

    private static final int[] CONST_IDS = new int[] { TransformC.ID, CameraC.ID };

    private ComponentReference<CameraC> cameraComponentRef;
    private Matrix4f projMatrix;

    public CameraGameObject(Context ctx) {
        super(ctx);

        EventBus eventBus = ctx.service(Services.EVENT_BUS);
        eventBus.register(this);
    }

    @Override
    protected void onSpawned() {
        this.cameraComponentRef = ecs.obtainComponentReference(entityId, CameraC.ID);
        var size = ctx.getEngine().getWindow().getSize();
        onWindowResize(new WindowResizeEvent(ctx.getEngine().getWindow(), size.width(), size.height()));
    }

    @SubscribeEvent
    public void onWindowResize(WindowResizeEvent event) {
        //PLEASE BRO i hate these one liners with the generic
        RenderSystem sys = ctx.<Renderer>service(Services.RENDERER).renderSystem();
        CameraC c = cameraComponentRef.getComponent();
        int idx = cameraComponentRef.getIndex();
        if (c.isOrtho[idx]) {// MAKE THESE PLSSSSS
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

    public Vector3f lookAt() {
        Vector3f forward = new Vector3f(0, 0, -1);
        getRotation().transform(forward);
        return forward;
    }

    public Matrix4f getProjectionMatrix() {
        return projMatrix;
    }

    public Matrix4f getViewMatrix() {
        return new Matrix4f()
                .rotate(getRotation().conjugate(new Quaternionf()))
                .translate(-getX(), -getY(), -getZ());
    }
}
